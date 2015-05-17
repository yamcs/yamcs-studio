package org.yamcs.studio.ui;

import java.util.TimeZone;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.yamcs.utils.TimeEncoding;

public class YamcsUIPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.ui";

    // The shared instance
    private static YamcsUIPlugin plugin;

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

    public static YamcsUIPlugin getDefault() {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(String path) {
        Bundle bundle = FrameworkUtil.getBundle(YamcsUIPlugin.class);
        return ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path(path), null));
    }

    public TimeZone getTimeZone() {
        // Currently always using local timezone, because need to hack into XYChart because
        // it doesn't support Timezones. Only date formats seem to be accounted for.
        // At least for now, it should stay consistent with the workbench
        // TODO Research modifications to SWT xychart and then make this controllable from user prefs
        return TimeZone.getDefault();
    }
}
