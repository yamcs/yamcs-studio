package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.yamcs.client.Page;
import org.yamcs.client.mdb.MissionDatabaseClient;
import org.yamcs.client.mdb.MissionDatabaseClient.ListOptions;
import org.yamcs.protobuf.Mdb.MemberInfo;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.ParameterWebSocketRequest;
import org.yamcs.studio.core.client.YamcsStudioClient;

/**
 * Keeps track of the parameter model. This does not currently include parameter values.
 */
public class ParameterCatalogue extends Catalogue {

    private static final Logger log = Logger.getLogger(ParameterCatalogue.class.getName());

    private Set<ParameterListener> parameterListeners = new CopyOnWriteArraySet<>();

    private List<ParameterInfo> metaParameters = Collections.emptyList();
    private Map<NamedObjectId, ParameterInfo> parametersById = new LinkedHashMap<>();

    // Index for faster repeat access
    private Map<NamedObjectId, String> unitsById = new ConcurrentHashMap<>();

    public static ParameterCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(ParameterCatalogue.class);
    }

    public void addParameterListener(ParameterListener listener) {
        parameterListeners.add(listener);
    }

    public void removeParameterListener(ParameterListener listener) {
        parameterListeners.remove(listener);
    }

    @Override
    public void changeInstance(String instance) {
        metaParameters = Collections.emptyList();
        unitsById.clear();

        if (instance != null) {
            loadMetaParameters();
        }
    }

    private synchronized void processMetaParameters(List<ParameterInfo> metaParameters) {
        log.info(String.format("Loaded %d parameters", metaParameters.size()));
        this.metaParameters = new ArrayList<>(metaParameters);
        this.metaParameters.sort((p1, p2) -> {
            return p1.getQualifiedName().compareTo(p2.getQualifiedName());
        });

        for (ParameterInfo p : this.metaParameters) {
            NamedObjectId id = NamedObjectId.newBuilder().setName(p.getQualifiedName()).build();
            parametersById.put(id, p);
            for (NamedObjectId alias : p.getAliasList()) {
                parametersById.put(alias, p);
            }

            // Update unit index
            if (p != null && p.hasType() && p.getType().getUnitSetCount() > 0) {
                String combinedUnit = p.getType().getUnitSet(0).getUnit();
                for (int i = 1; i < p.getType().getUnitSetCount(); i++) {
                    combinedUnit += " " + p.getType().getUnitSet(i).getUnit();
                }
                unitsById.put(id, combinedUnit);
            }
        }

        parameterListeners.forEach(ParameterListener::mdbUpdated);
    }

    private void loadMetaParameters() {
        Job job = Job.create("Loading parameters", monitor -> {
            log.fine("Fetching available parameters");
            MissionDatabaseClient mdb = YamcsPlugin.getMissionDatabaseClient();
            List<ParameterInfo> parameters = new ArrayList<>();

            if (mdb != null) {
                try {
                    Page<ParameterInfo> page = mdb.listParameters(ListOptions.limit(500)).get();
                    page.iterator().forEachRemaining(parameters::add);
                    while (page.hasNextPage()) {
                        page = page.getNextPage().get();
                        page.iterator().forEachRemaining(parameters::add);
                    }
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    log.log(Level.SEVERE, "Exception while loading parameters: " + cause.getMessage(), cause);
                    return Status.OK_STATUS;
                }
            }
            processMetaParameters(parameters);
            return Status.OK_STATUS;
        });
        job.setPriority(Job.LONG);
        job.schedule(1000L);
    }

    public void subscribeParameters(NamedObjectList idList) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        if (yamcsClient.isConnected()) {
            String instance = ManagementCatalogue.getCurrentYamcsInstance();
            if (instance != null) {
                yamcsClient.subscribe(new ParameterWebSocketRequest("subscribe", idList), this);
            }
        }
    }

    @Override
    public void onMessage(WebSocketSubscriptionData msg) {
        if (msg.hasParameterData()) {
            ParameterData pdata = msg.getParameterData();
            log.finest(String.format("Sending %s parameters to %s listeners",
                    pdata.getParameterCount(), parameterListeners.size()));
            parameterListeners.forEach(l -> l.onParameterData(pdata));
        }
    }

    public void unsubscribeParameters(NamedObjectList idList) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        if (yamcsClient.isConnected()) {
            String instance = ManagementCatalogue.getCurrentYamcsInstance();
            if (instance != null) {
                yamcsClient.sendWebSocketMessage(new ParameterWebSocketRequest("unsubscribe", idList));
            }
        }
    }

    /**
     * Returns the ParameterInfo for an ID, the ID may also point to an aggregate member or an array entry, the returned
     * ParameterInfo will then match the containing parameter.
     */
    public ParameterInfo getParameterInfo(NamedObjectId id) {
        String[] parts = removeArrayAndAggregateOffset(id.getName());
        if (parts[1] != null) {
            id = NamedObjectId.newBuilder(id).setName(parts[0]).build();
        }
        return parametersById.get(id);
    }

    /**
     * Returns the ParameterTypeInfo for an ID, the ID may also point to an aggregate member or an array entry, the
     * returned ParameterInfo will then match that specific path into the parameter.
     */
    public ParameterTypeInfo getParameterTypeInfo(NamedObjectId id) {
        String suffix = removeArrayAndAggregateOffset(id.getName())[1];

        ParameterInfo parameter = getParameterInfo(id);
        if (parameter == null) {
            return null;
        }

        String qualifiedNameWithSuffix = parameter.getQualifiedName();
        if (suffix != null) {
            qualifiedNameWithSuffix += suffix;
        }

        return findMatchingParameterType(parameter.getType(), parameter.getQualifiedName(), qualifiedNameWithSuffix);
    }

    private ParameterTypeInfo findMatchingParameterType(ParameterTypeInfo parent, String parentName,
            String qualifiedNameWithSuffix) {
        if (parent == null) {
            return null;
        } else if (qualifiedNameWithSuffix.matches(parentName)) {
            return parent;
        } else {
            for (MemberInfo member : parent.getMemberList()) {
                ParameterTypeInfo memberType = member.getType();
                String name = parentName + "." + member.getName();
                ParameterTypeInfo match = findMatchingParameterType(memberType, name, qualifiedNameWithSuffix);
                if (match != null) {
                    return match;
                }
            }
            if (parent.hasArrayInfo()) {
                ParameterTypeInfo entryType = parent.getArrayInfo().getType();
                String name = parentName + "\\[[0-9]+\\]";
                ParameterTypeInfo match = findMatchingParameterType(entryType, name, qualifiedNameWithSuffix);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    public String getCombinedUnit(NamedObjectId id) {
        return unitsById.get(id);
    }

    public List<ParameterInfo> getMetaParameters() {
        return new ArrayList<>(metaParameters);
    }

    /**
     * Splits a PV name into the actual parameter name, and the struct or array path within that parameter.
     * 
     * For example: "/bla/bloe.f[3].heh" becomes { "/bla/bloe", ".f[3].heh" }
     */
    private static String[] removeArrayAndAggregateOffset(String name) {
        int searchFrom = name.lastIndexOf('/');

        int trimFrom = -1;

        int arrayStart = name.indexOf('[', searchFrom);
        if (arrayStart != -1) {
            trimFrom = arrayStart;
        }

        int memberStart = name.indexOf('.', searchFrom);
        if (memberStart != -1) {
            if (trimFrom >= 0) {
                trimFrom = Math.min(trimFrom, memberStart);
            } else {
                trimFrom = memberStart;
            }
        }

        if (trimFrom >= 0) {
            return new String[] { name.substring(0, trimFrom), name.substring(trimFrom) };
        } else {
            return new String[] { name, null };
        }
    }
}
