package org.csstudio.utility.pvmanager.yamcs;

import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.vtype.YamcsVTypeAdapter;
import org.csstudio.utility.pvmanager.yamcs.service.YPVListener;
import org.csstudio.utility.pvmanager.yamcs.service.YRegistrar;
import org.epics.pvmanager.ChannelWriteCallback;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.MultiplexedChannelHandler;
import org.epics.pvmanager.ValueCache;
import org.yamcs.protobuf.ParameterValue;

public class YamcsPVChannelHandler extends MultiplexedChannelHandler<Boolean, ParameterValue> implements YPVListener {
    
    private YRegistrar registrar;
    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();
    private static final Logger log = Logger.getLogger(YamcsPVChannelHandler.class.getName());

    public YamcsPVChannelHandler(String channelName, YRegistrar registrar) {
        super(channelName);
        this.registrar = registrar;
    }

    /**
     * Called for every first read/write on a channel
     */
    @Override
    protected void connect() { // Interpret this as a subscribe
        log.fine("Connect called on " + getChannelName());
        registrar.connectChannelHandler(this);
        processConnection(Boolean.TRUE);
    }
    
    @Override
    public String getPVName() {
        return getChannelName();
    }

    /**
     * This gets called when a channel has no more active readers. This could
     * also happen while in the same OPI runtime session. So don't close
     * the websocket here.
     * TODO maybe just unsubscribe, and close websocket after timeout?
     */
    @Override
    protected void disconnect() { // Interpret this as an unsubscribe
        registrar.disconnectChannelHandler(this);
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
    @Override
    public void processParameterValue(ParameterValue pval) {
        processMessage(pval);
    }
    
    @Override
    protected DataSourceTypeAdapter<Boolean, ParameterValue> findTypeAdapter(
            ValueCache<?> cache, Boolean connection) {
        return TYPE_ADAPTER;
    }
    
    @Override
    public void signalYamcsConnected() {
        processConnection(Boolean.TRUE);
    }
    
    @Override
    public void signalYamcsDisconnected() {
        processConnection(Boolean.FALSE);
    }
    
    @Override
    public void reportException(Exception e) { // Expose protected method
        reportExceptionToAllReadersAndWriters(e);
    }
}
