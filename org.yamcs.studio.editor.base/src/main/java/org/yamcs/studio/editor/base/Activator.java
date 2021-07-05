package org.yamcs.studio.editor.base;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.YamcsPlugin;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.editor";

    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // Trigger other bundles
        YamcsPlugin.getDefault();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }
}
