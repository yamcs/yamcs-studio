package org.csstudio.utility.pvmanager.yamcs;

import org.csstudio.utility.pvmanager.yamcs.service.YService;
import org.epics.pvmanager.ChannelHandler;
import org.epics.pvmanager.DataSource;

/**
 * TODO When running the OPIbuilder, it seems like this is instantiated for
 * every parameter separately. Which probably also explains why the ReadRecipe
 * only contains one channel at a time.
 */
public class YamcsPVDataSource extends DataSource {

    /*static {
        // Install type support for the types it generates
        DataTypeSupport.install();
    }*/

    private static YService yservice = new YService();

    public YamcsPVDataSource() {
        super(false);
    }

    @Override
    public void close() { // hmm not called on opiruntime close..
        yservice.shutdown();
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new YamcsPVChannelHandler(channelName, yservice);
    }
}
