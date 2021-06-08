package org.yamcs.studio.css.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.yamcs.YamcsSubscriptionService;

public class Activator extends AbstractUIPlugin {

    private static BundleContext bundleContext;
    private static Activator plugin;

    private Beeper beeper;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        plugin = this;
        beeper = new Beeper();
        YamcsSubscriptionService service = YamcsPlugin.getService(YamcsSubscriptionService.class);
        service.addParameterValueListener(beeper);
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

    public Beeper getBeeper() {
        return beeper;
    }
}
