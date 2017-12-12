package org.yamcs.studio.css;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.ParameterCatalogue;

public class Activator extends AbstractUIPlugin {

    private static Activator plugin;

    @SuppressWarnings("unused")
    private SeverityHandlerSound severityHandler;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        PVCatalogue pvCatalogue = new PVCatalogue();
        YamcsPlugin.getDefault().registerCatalogue(pvCatalogue);
        ParameterCatalogue.getInstance().addParameterListener(pvCatalogue);

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
}
