package org.yamcs.studio.core.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.vtype.YamcsVType;
import org.yamcs.utils.TimeEncoding;

public class YamcsUIPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.core.ui";

    private static YamcsUIPlugin plugin;

    public static final String CMD_CONNECT = "org.yamcs.studio.ui.connect";

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        TimeEncoding.setUp();
        ConnectionUIHelper.getInstance();
        YamcsVType.severityHandler = new SeverityHandlerSound();
        DisplayOpener.init();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static YamcsUIPlugin getDefault() {
        return plugin;
    }
}
