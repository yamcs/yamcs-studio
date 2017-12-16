package org.yamcs.studio.css.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

    private static BundleContext bundleContext;
    private static Activator plugin;

    @SuppressWarnings("unused")
    private SeverityHandlerSound severityHandler;

    private PVCatalogue pvCatalogue;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        plugin = this;

        pvCatalogue = new PVCatalogue();
        severityHandler = new SeverityHandlerSound();
        DisplayOpener.init();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public PVCatalogue getPVCatalogue() {
        return pvCatalogue;
    }
}
