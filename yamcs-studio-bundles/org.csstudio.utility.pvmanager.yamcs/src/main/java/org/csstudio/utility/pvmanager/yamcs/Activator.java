package org.csstudio.utility.pvmanager.yamcs;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * Currently we only use it as an easy way to get to the Bundle-Version
 * as declared in the manifest.
 */
public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.csstudio.utility.pvmanager.yamcs";

	// The shared instance
	private static Activator plugin;
	
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

	public static Activator getDefault() {
		return plugin;
	}
}
