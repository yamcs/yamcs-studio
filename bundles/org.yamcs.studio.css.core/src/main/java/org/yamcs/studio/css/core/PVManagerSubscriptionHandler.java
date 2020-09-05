package org.yamcs.studio.css.core;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Keeps track of {@link ParameterChannelHandler} registration state and takes care of establishing or re-establishing a
 * bundled parameter subscription against Yamcs.
 */
public class PVManagerSubscriptionHandler implements YamcsAware, ParameterSubscription.Listener {

    private static final Logger log = Logger.getLogger(PVManagerSubscriptionHandler.class.getName());

    // Change events (PV handler register/unregister). Only relevant if a connection
    // is ongoing (to prevent bursts)
    /// private Queue<ChannelEvent> channelHandlerEvents = new ConcurrentLinkedQueue<>();

    // All currently registered PV handlers (regardless of connection state to Yamcs)
    /// private Map<NamedObjectId, ParameterChannelHandler> channelHandlersById = new LinkedHashMap<>();

    private ParameterSubscription subscription;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Beeper beeper;

    PVManagerSubscriptionHandler() {
        beeper = new Beeper();
        executor.scheduleWithFixedDelay(this::modifySubscription, 500, 500, TimeUnit.MILLISECONDS);
        YamcsPlugin.addListener(this);
    }

    public static PVManagerSubscriptionHandler getInstance() {
        return Activator.getDefault().getPVCatalogue();
    }

    public Beeper getBeeper() {
        return beeper;
    }

    // To be called on executor thread
    private void createSubscription(String instance, String processor) {
        /// channelHandlerEvents.clear();
        /*subscription = YamcsPlugin.getYamcsClient().createParameterSubscription();
        subscription.addListener(this);
        subscription.sendMessage(SubscribeParametersRequest.newBuilder()
                .setInstance(instance)
                .setProcessor(processor)
                .setAbortOnInvalid(false)
                .addAllId(channelHandlersById.keySet())
                .build());*/
    }

    // To be called on executor thread
    private void modifySubscription() {
        /*if (subscription != null && channelHandlerEvents.peek() != null) {
            ChannelEvent firstEvent = channelHandlerEvents.poll();
            Set<ParameterChannelHandler> newHandlers = new HashSet<>();
            newHandlers.add(firstEvent.handler);
        
            while (channelHandlerEvents.peek() != null
                    && channelHandlerEvents.peek().registered == firstEvent.registered) {
                ChannelEvent event = channelHandlerEvents.poll();
                newHandlers.add(event.handler);
            }
        
            if (firstEvent.registered) {
                newHandlers.forEach(handler -> {
                    ParameterInfo parameter = YamcsPlugin.getMissionDatabase().getParameterInfo(handler.getId());
                    handler.processConnectionInfo(new PVConnectionInfo(true, parameter));
                });
                subscription.add(newHandlers.stream().map(handler -> handler.getId()).collect(Collectors.toList()));
            } else {
                subscription.remove(newHandlers.stream().map(handler -> handler.getId()).collect(Collectors.toList()));
            }
        }*/
    }

    @Override
    public void changeProcessor(String instance, String processor) {
        Display.getDefault().syncExec(() -> {
            if (subscription != null) {
                subscription.cancel(true);
                subscription = null;
            }

            // Triggers an 'unregister' and 'register' on all channels
            // We must do this, because it's the only way to get a fully clean display
            // Unfortunately:
            // - it will also flash a bit due to the state changes
            // - it does not stay on calling thread
            OPIUtils.resetDisplays();
            // Avoid concurrent modification caused by a (un)register event
            // triggered by resetDisplays.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            // Clear internal state of channel handlers
            /// channelHandlersById.forEach((id, channelHandler) -> channelHandler.resetMessage());

            /*
            if (processor == null) {
                channelHandlersById.forEach((id, channelHandler) -> {
                    channelHandler.processConnectionInfo(new PVConnectionInfo(false, null));
                });
            } else {
                executor.execute(() -> createSubscription(instance, processor));
                channelHandlersById.forEach((id, channelHandler) -> {
                    ParameterInfo parameter = YamcsPlugin.getMissionDatabase().getParameterInfo(id);
                    channelHandler.processConnectionInfo(new PVConnectionInfo(true, parameter));
                });
            }*/
        });
    }

    /*public synchronized void register(ParameterChannelHandler channelHandler) {
        channelHandlersById.put(channelHandler.getId(), channelHandler);
        channelHandlerEvents.add(new ChannelEvent(channelHandler, true));
    }
    
    public synchronized void unregister(ParameterChannelHandler channelHandler) {
        channelHandlersById.remove(channelHandler.getId());
        channelHandlerEvents.add(new ChannelEvent(channelHandler, false));
    }
    
    private static class ChannelEvent {
        ParameterChannelHandler handler;
        boolean registered;
    
        ChannelEvent(ParameterChannelHandler handler, boolean registered) {
            this.handler = handler;
            this.registered = registered;
        }
    }*/

    public void stop() {
        executor.shutdown();
    }

    @Override
    public void onData(List<ParameterValue> values) {
        /*for (ParameterValue pval : values) {
            ParameterChannelHandler channelHandler = channelHandlersById.get(pval.getId());
            if (channelHandler != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer(String.format("Request to update channel %s to %s", channelHandler.getId().getName(),
                            StringConverter.toString(pval.getEngValue())));
                }
                channelHandler.processParameterValue(pval);
            }
        }
        beeper.processDelivery(values);*/
    }
}
