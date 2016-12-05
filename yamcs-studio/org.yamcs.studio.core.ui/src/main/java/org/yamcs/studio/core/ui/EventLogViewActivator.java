package org.yamcs.studio.core.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.EventListener;

public class EventLogViewActivator implements EventListener {

    private static final Logger log = Logger.getLogger(EventLogViewActivator.class.getName());

    private EventLogViewActivator() {
        EventCatalogue.getInstance().addEventListener(this);
    }

    public static void init() {
        new EventLogViewActivator();
    }

    @Override
    public void processEvent(Event event) {
        Display.getDefault().asyncExec(() -> {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.yamcs.studio.ui.eventlog.EventLogView");
            } catch (PartInitException e) {
                log.log(Level.WARNING, "Failed to init part", e);
            }
        });
    }
}
