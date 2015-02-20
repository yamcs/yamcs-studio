package org.csstudio.utility.pvmanager.yamcs;

import org.csstudio.platform.libs.yamcs.vtype.YamcsVTypeAdapter;
import org.csstudio.utility.pvmanager.yamcs.service.YService;
import org.epics.pvmanager.ChannelWriteCallback;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.MultiplexedChannelHandler;
import org.epics.pvmanager.ValueCache;
import org.yamcs.protobuf.ParameterValue;

public class YamcsPVChannelHandler extends MultiplexedChannelHandler<Boolean, ParameterValue> {
    
    private YService yservice;
    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();

    public YamcsPVChannelHandler(String channelName, YService yservice) {
        super(channelName);
        this.yservice = yservice;
    }

    /**
     * Called for every first read/write on a channel
     */
    @Override
    protected void connect() {
        System.out.println("Connect called on " + getChannelName());
        yservice.connectChannelHandler(this);
        processConnection(Boolean.TRUE);
    }

    /**
     * This gets called when a channel has no more active readers. This could
     * also happen while in the same OPI runtime session. So don't close
     * the websocket here.
     * TODO maybe just unsubscribe, and close websocket after timeout?
     */
    @Override
    protected void disconnect() {
        yservice.disconnectChannelHandler(this);
        // Not good: yservice.disconnect();
    }
    
    @Override
    protected boolean isConnected(Boolean connected) {
        return connected != null && connected.booleanValue();
    }

    @Override
    protected void write(Object newValue, ChannelWriteCallback callback) {
        throw new UnsupportedOperationException("Channel write not supported");
    }
    
    /**
     * Process a parameter value update to be send to the display
     */
    public void processParameterValue(ParameterValue pval) {
        processMessage(pval);
    }
    
    @Override
    protected DataSourceTypeAdapter<Boolean, ParameterValue> findTypeAdapter(
            ValueCache<?> cache, Boolean connection) {
        return TYPE_ADAPTER;
    }
    
    public void signalYamcsConnected() {
        processConnection(Boolean.TRUE);
    }
    
    public void signalYamcsDisconnected() {
        processConnection(Boolean.FALSE);
    }
    
    public void reportException(Exception e) { // Expose protected method
        reportExceptionToAllReadersAndWriters(e);
    }
}
