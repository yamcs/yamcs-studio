package org.yamcs.studio.css.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

    private static BundleContext bundleContext;
    private static Activator plugin;

    @SuppressWarnings("unused")
    private SeverityHandlerSound severityHandler;

    private PVManagerSubscriptionHandler pvCatalogue;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        plugin = this;

        pvCatalogue = new PVManagerSubscriptionHandler();
        severityHandler = new SeverityHandlerSound();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        pvCatalogue.stop();
        super.stop(context);
        plugin = null;
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public PVManagerSubscriptionHandler getPVCatalogue() {
        return pvCatalogue;
    }
}
