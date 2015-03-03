package org.csstudio.vtype.pv.yamcs;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.YPVListener;
import org.csstudio.platform.libs.yamcs.YRegistrar;
import org.csstudio.platform.libs.yamcs.YamcsConnectionProperties;
import org.csstudio.platform.libs.yamcs.YamcsPlugin;
import org.csstudio.platform.libs.yamcs.vtype.YamcsVType;
import org.csstudio.vtype.pv.PV;
import org.yamcs.protostuff.ParameterValue;

public class Yamcs_PV extends PV implements YPVListener {
    
    private static final Logger log = Logger.getLogger(Yamcs_PV.class.getName());
    private YRegistrar registrar;
    private String baseName;

    protected Yamcs_PV(String name, String baseName) {
        super(name);
        this.baseName = baseName;
        
        // Notify that this PV is read-only
        notifyListenersOfPermissions(true);
        
        String yamcsHost = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_host");
        int yamcsPort = YamcsPlugin.getDefault().getPreferenceStore().getInt("yamcs_port");
        String yamcsInstance = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_instance");
        registrar = YRegistrar.getInstance(new YamcsConnectionProperties(yamcsHost, yamcsPort, yamcsInstance));
        registrar.connectChannelHandler(this);
    }

    @Override
    public void write(Object newValue) throws Exception {
        throw new UnsupportedOperationException("No write supported on yamcs PVs");
    }

    @Override
    public void signalYamcsConnected() {
        // TODO don't need to report this to PV listeners?
    }

    @Override
    public void signalYamcsDisconnected() {
        notifyListenersOfDisconnect();
    }

    @Override
    public void reportException(Exception e) {
        // TODO Can't be reported to listeners?
        log.log(Level.SEVERE, "Exception reported on PV " + getName(), e);
    }

    @Override
    public String getPVName() {
        return baseName;
    }

    @Override
    public void processParameterValue(ParameterValue pval) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Incoming PV update of " + pval.getId().getName() + " at " + pval.getAcquisitionTimeUTC());
        }
        notifyListenersOfValue(YamcsVType.fromYamcs(pval));
    }
    
    /**
     * Called by PVPool. Closes the PV releasing underlying resources.
     */
    @Override
    protected void close() {
        super.close();
        registrar.disconnectChannelHandler(this);
    }
}
