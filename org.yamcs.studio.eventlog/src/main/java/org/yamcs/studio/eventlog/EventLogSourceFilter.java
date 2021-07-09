package org.yamcs.studio.eventlog;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Combo;
import org.yamcs.protobuf.Yamcs.Event;

/**
 * Allows the user to select the source for the events managed by EventLogTableViewer from a dropdown Combo box.
 * Scoped to the package as this should only be used in the org.yamcs.studio.eventlog package.
 * @author lgomez
 *
 */
class EventLogSourceFilter extends ViewerFilter {
    private LinkedHashSet<String> eventSources = new LinkedHashSet<String>(); // Not sure if this is needed anymore
    private Combo sourceCombo;
    private final String ANY_SOURCE = "Any ";

    /**
     *
     * @param sourceCombo The combo box which will be populated with the items(such as event sources)
     * in the select filter.
     * @apiNote The sourceCombo's items are set to the string ANY_SOURCE. Meaning that items previously set
     * in this combo box will be overwritten.
     */
    public EventLogSourceFilter(Combo sourceCombo) {
        this.sourceCombo = sourceCombo;
        sourceCombo.setItems(ANY_SOURCE);
        sourceCombo.select(0);
    }

    public String[] getEventSources() {
        return Arrays.copyOf(eventSources.toArray(), eventSources.size(), String[].class);
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof EventLogItem) {
            Event event = ((EventLogItem) element).event;
            if (!eventSources.contains(event.getSource())) {
                eventSources.add(event.getSource());
                sourceCombo.add(event.getSource());
            }
            if (event.getSource().equals(sourceCombo.getText())) {
                return true;
            } else {
                if (sourceCombo.getText().equals(ANY_SOURCE)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
