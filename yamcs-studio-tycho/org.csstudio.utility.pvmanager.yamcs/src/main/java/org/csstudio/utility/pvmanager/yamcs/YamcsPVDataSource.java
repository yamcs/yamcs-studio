package org.csstudio.utility.pvmanager.yamcs;

import org.csstudio.platform.libs.yamcs.YamcsConnectionProperties;
import org.csstudio.platform.libs.yamcs.ui.YamcsUIPlugin;
import org.csstudio.utility.pvmanager.yamcs.service.YRegistrar;
import org.epics.pvmanager.ChannelHandler;
import org.epics.pvmanager.DataSource;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class YamcsPVDataSource extends DataSource {

    private static YRegistrar registrar;

    public YamcsPVDataSource() {
        super(false);
        String yamcsHost = YamcsUIPlugin.getDefault().getPreferenceStore().getString("yamcs_host");
        int yamcsPort = YamcsUIPlugin.getDefault().getPreferenceStore().getInt("yamcs_port");
        String yamcsInstance = YamcsUIPlugin.getDefault().getPreferenceStore().getString("yamcs_instance");
        registrar = YRegistrar.getInstance(new YamcsConnectionProperties(yamcsHost, yamcsPort, yamcsInstance));
    }

    @Override
    public void close() { // hmm not called on opiruntime close..
        registrar.shutdown();
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new YamcsPVChannelHandler(channelName, registrar);
    }
}
