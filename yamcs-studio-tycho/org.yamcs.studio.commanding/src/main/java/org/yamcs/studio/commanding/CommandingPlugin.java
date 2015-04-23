package org.yamcs.studio.commanding;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.yamcs.utils.TimeEncoding;

/**
 * Just something temporary to correclty interpet yamcs time
 */
public class CommandingPlugin extends Plugin {
    public static final String PLUGIN_ID = "org.csstudio.yamcs.commanding";

    // The shared instance
    private static CommandingPlugin plugin;
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        TimeEncoding.setUp();
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static CommandingPlugin getDefault() {
        return plugin;
    }
}
