package org.yamcs.studio.data.yamcs;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.SubscribeParametersRequest;
import org.yamcs.protobuf.SubscribeParametersRequest.Action;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.PluginService;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.vtype.VType;

/**
 * Keeps track of {@link IPV} registration state and takes care of establishing or re-establishing a bundled parameter
 * subscription against Yamcs.
 */
public class YamcsSubscriptionService implements YamcsAware, ParameterSubscription.Listener, PluginService {

    private static final Logger log = Logger.getLogger(YamcsSubscriptionService.class.getName());

    private Map<NamedObjectId, Set<IPV>> pvsById = new LinkedHashMap<>();

    private ParameterSubscription subscription;
    private AtomicBoolean subscriptionDirty = new AtomicBoolean(false);
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Set<ParameterValueListener> parameterValueListeners = new HashSet<>();

    public YamcsSubscriptionService() {

        // Periodically check if the subscription needs a refresh
        // (PVs send individual events, so this bundles them)
        executor.scheduleWithFixedDelay(() -> {
            if (subscriptionDirty.getAndSet(false) && subscription != null) {
                Set<NamedObjectId> ids = getRequestedIdentifiers();
                log.fine(String.format("Modifying subscription to %s", ids));
                subscription.sendMessage(SubscribeParametersRequest.newBuilder()
                        .setAction(Action.REPLACE)
                        .setSendFromCache(true)
                        .setAbortOnInvalid(false)
                        .setUpdateOnExpiration(true)
                        .addAllId(ids)
                        .build());
            }
        }, 500, 500, TimeUnit.MILLISECONDS);

        YamcsPlugin.addListener(this);
    }

    private Set<NamedObjectId> getRequestedIdentifiers() {
        return pvsById.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    public boolean isSubscriptionAvailable() {
        return subscription != null;
    }

    public VType getValue(String pvName) {
        NamedObjectId id = identityOf(pvName);
        if (subscription != null) {
            ParameterValue pval = subscription.get(id);
            if (pval != null) {
                boolean raw = pvName.startsWith("raw://");
                return YamcsVType.fromYamcs(pval, raw);
            }
        }
        return null;
    }

    @Override
    public void changeProcessor(String instance, String processor) {
        executor.execute(() -> {
            if (subscription != null) {
                subscription.cancel(true);
                subscription = null;
                pvsById.forEach((id, pvs) -> {
                    pvs.forEach(pv -> {
                        pv.notifyConnectionChange();
                        pv.notifyValueChange();
                        pv.notifyWritePermissionChange();
                    });
                });
            }

            if (processor != null) {
                subscription = YamcsPlugin.getYamcsClient().createParameterSubscription();
                subscription.addListener(this);

                // Reset connection and value state
                pvsById.forEach((id, pvs) -> {
                    pvs.forEach(pv -> {
                        pv.notifyConnectionChange();
                        pv.notifyValueChange();
                        pv.notifyWritePermissionChange();
                    });
                });

                // Ready to receive some data
                Set<NamedObjectId> ids = getRequestedIdentifiers();
                log.fine(String.format("Subscribing to %s [%s/%s]", ids, instance, processor));
                subscription.sendMessage(SubscribeParametersRequest.newBuilder()
                        .setInstance(instance)
                        .setProcessor(processor)
                        .setSendFromCache(true)
                        .setAbortOnInvalid(false)
                        .setUpdateOnExpiration(true)
                        .addAllId(ids)
                        .build());
            }
        });
    }

    /**
     * Async adds a Yamcs PV for receiving updates.
     */
    public void register(IPV pv) {
        NamedObjectId id = identityOf(pv.getName());
        executor.execute(() -> {
            Set<IPV> pvs = pvsById.computeIfAbsent(id, x -> new HashSet<>());
            pvs.add(pv);
            subscriptionDirty.set(true);
        });
    }

    /**
     * Async removes a Yamcs PV from receiving updates.
     */
    public void unregister(IPV pv) {
        NamedObjectId id = identityOf(pv.getName());
        executor.execute(() -> {
            Set<IPV> pvs = pvsById.get(id);
            if (pvs != null) {
                boolean removed = pvs.remove(pv);
                if (removed) {
                    subscriptionDirty.set(true);
                }
            }
        });
    }

    @Override
    public void dispose() {
        YamcsPlugin.removeListener(this);
        executor.shutdown();
    }

    @Override
    public void onData(List<ParameterValue> values) {
        executor.execute(() -> {
            for (ParameterValue pval : values) {
                Set<IPV> pvs = pvsById.get(pval.getId());
                if (pvs != null) {
                    pvs.forEach(pv -> pv.notifyValueChange());
                }
            }
            parameterValueListeners.forEach(l -> l.onData(values));
        });
    }

    public void addParameterValueListener(ParameterValueListener listener) {
        parameterValueListeners.add(listener);
    }

    @Override
    public void onInvalidIdentification(NamedObjectId id) {
        executor.execute(() -> {
            // We keep the id in pvsById, we want to again receive the invalid
            // identification when the subscription is updated.
            Set<IPV> pvs = pvsById.get(id);
            if (pvs != null) {
                pvs.forEach(IPV::setInvalid);
            }
        });
    }

    public static NamedObjectId identityOf(String pvName) {
        if (pvName.startsWith("ops://")) {
            return NamedObjectId.newBuilder()
                    .setNamespace("MDB:OPS Name")
                    .setName(pvName.substring("ops://".length()))
                    .build();
        } else if (pvName.startsWith("para://")) {
            return NamedObjectId.newBuilder()
                    .setName(pvName.substring("para://".length()))
                    .build();
        } else if (pvName.startsWith("raw://")) {
            return NamedObjectId.newBuilder()
                    .setName(pvName.substring("raw://".length()))
                    .build();
        } else {
            return NamedObjectId.newBuilder()
                    .setName(pvName)
                    .build();
        }
    }

    @FunctionalInterface
    public static interface ParameterValueListener {
        void onData(List<ParameterValue> values);
    }
}
