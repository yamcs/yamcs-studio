package org.yamcs.studio.ui.css;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private static BundleContext bundleContext;

    @Override
    public void start(BundleContext context) throws Exception {
        bundleContext = context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }
}
