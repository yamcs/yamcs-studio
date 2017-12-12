package org.yamcs.studio.css.core;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;

public class Activator extends AbstractUIPlugin implements InstanceListener {

    private static BundleContext bundleContext;
    private static Activator plugin;

    @SuppressWarnings("unused")
    private SeverityHandlerSound severityHandler;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        plugin = this;

        ManagementCatalogue.getInstance().addInstanceListener(this);

        PVCatalogue pvCatalogue = new PVCatalogue();
        YamcsPlugin.getDefault().registerCatalogue(pvCatalogue);
        ParameterCatalogue.getInstance().addParameterListener(pvCatalogue);

        severityHandler = new SeverityHandlerSound();
        DisplayOpener.init();
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        // TODO verify behaviour. Maybe we should have a beforeInstanceChange
        // and an after to get the correct pv connection state
        Display.getDefault().asyncExec(() -> {
            OPIUtils.resetDisplays();
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;

        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        if (catalogue != null) {
            catalogue.removeInstanceListener(this);
        }
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }
}
