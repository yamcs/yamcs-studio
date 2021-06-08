package org.yamcs.studio.commanding;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CommandingPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.commanding";

    private static CommandingPlugin plugin;

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

    public static CommandingPlugin getDefault() {
        return plugin;
    }

    public ImageDescriptor getImageDescriptor(String path) {
        return ImageDescriptor.createFromURL(getBundle().getEntry(path));
    }

    public IDialogSettings getCommandHistoryTableSettings() {
        IDialogSettings settings = getDialogSettings();
        IDialogSettings section = settings.getSection("cmdhist-table");
        if (section == null) {
            section = settings.addNewSection("cmdhist-table");
        }
        return section;
    }
}
