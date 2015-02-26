package org.csstudio.platform.libs.yamcs.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class YamcsUIPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.csstudio.utility.platform.libs.yamcs.ui";

    // The shared instance
    private static YamcsUIPlugin plugin;
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static YamcsUIPlugin getDefault() {
        return plugin;
    }
}
