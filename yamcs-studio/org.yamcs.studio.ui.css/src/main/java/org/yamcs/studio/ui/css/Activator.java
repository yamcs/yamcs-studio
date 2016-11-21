package org.yamcs.studio.ui.css;

import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;

public class Activator implements BundleActivator, InstanceListener {

    private static BundleContext bundleContext;

    @Override
    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        ManagementCatalogue.getInstance().addInstanceListener(this);
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
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        if (catalogue != null) {
            catalogue.removeInstanceListener(this);
        }
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }
}
