package org.yamcs.studio.product;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

public class ProductPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.product.default";

    private static ProductPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // Trigger other bundles
        YamcsUIPlugin.getDefault();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static ProductPlugin getDefault() {
        return plugin;
    }

}
