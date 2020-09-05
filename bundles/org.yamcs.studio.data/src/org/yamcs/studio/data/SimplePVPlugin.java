package org.yamcs.studio.data;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SimplePVPlugin implements BundleActivator {

    public static final String PLUGIN_ID = "org.yamcs.studio.data";

    @Override
    public void start(BundleContext context) throws Exception {
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (PVFactory.SIMPLE_PV_THREAD != null) {
            PVFactory.SIMPLE_PV_THREAD.shutdown();
        }
    }
}
