package org.yamcs.studio.eventlog;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.eventlog";

    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    public static Activator getDefault() {
        return plugin;
    }

    public ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public IDialogSettings getCommandHistoryTableSettings() {
        IDialogSettings settings = getDialogSettings();
        IDialogSettings section = settings.getSection("eventlog-table");
        if (section == null) {
            section = settings.addNewSection("eventlog-table");
        }
        return section;
    }
}
