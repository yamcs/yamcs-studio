package org.csstudio.utility.pvmanager.yamcs;

import java.util.logging.Logger;

import org.csstudio.autocomplete.AutoCompleteHelper;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * Currently we only use it as an easy way to get to the Bundle-Version
 * as declared in the manifest.
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.csstudio.utility.pvmanager.yamcs";
	private static final Logger log = Logger.getLogger(Activator.class.getName());

	// The shared instance
	private static Activator plugin;
	
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		// TODO This is a bit of a hack to get yamcs:// datasource registered early on. Surely there's a better way?
        log.info("Registering datasources early on:");
        for (String prefix : AutoCompleteHelper.retrievePVManagerSupported()) {
            log.info(" - Preloaded support for " + prefix);
        }
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
