package org.yamcs.studio.core;

import java.util.Objects;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;

public class DisplayService implements YamcsAware, PluginService {

    private static final Logger log = Logger.getLogger(DisplayService.class.getName());

    private String previousInstance;
    private String previousProcessor;

    public DisplayService() {
        YamcsPlugin.addListener(this);
    }

    @Override
    public void changeProcessor(String instance, String processor) {
        // Reduce the number of events
        boolean realChange = !Objects.equals(instance, previousInstance)
                || !Objects.equals(processor, previousProcessor);

        previousInstance = instance;
        previousProcessor = processor;

        if (realChange) {
            // What we really want is that all the widgets lose their values, so
            // that they wouldn't get restored on another processor or connection.

            // But from the way CSS-BOY is built, the only way to achieve that
            // is to reset the displays. (widgets ignore "null" value updates)
            // and only show a 'disconnected' frame.

            Display display = Display.getDefault();
            if (display != null && !display.isDisposed()) {
                display.asyncExec(() -> {
                    log.fine("No processor: resetting display state");
                    RCPUtils.runCommand("org.csstudio.opibuilder.refreshAllDisplays");
                });
            }
        }
    }

    @Override
    public void dispose() {
        YamcsPlugin.removeListener(this);
    }
}
