package org.yamcs.studio.eventlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Combo;
import org.yamcs.protobuf.Yamcs.Event;

/**
 * Allows the user to select the source for the events managed by EventLogTableViewer from a dropdown Combo box.
 */
public class EventLogSourceFilter extends ViewerFilter {
    private static final String ANY_SOURCE = "Any ";

    private Set<String> eventSources = new HashSet<>();
    private Combo sourceCombo;

    /**
     * @param sourceCombo
     *            The combo box which will be populated with the items (such as event sources) in the select filter.
     * @apiNote The sourceCombo's items are set to the string ANY_SOURCE. Meaning that items previously set in this
     *          combo box will be overwritten.
     */
    public EventLogSourceFilter(Combo sourceCombo) {
        this.sourceCombo = sourceCombo;
        sourceCombo.setItems(ANY_SOURCE);
        sourceCombo.select(0);
    }

    public void clear() {
        eventSources.clear();
        sourceCombo.setItems(ANY_SOURCE);
        sourceCombo.select(0);
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof EventLogItem) {
            Event event = ((EventLogItem) element).event;
            if (!eventSources.contains(event.getSource())) {
                eventSources.add(event.getSource());

                // Keep choices sorted, while preserving an existing selection
                int selectionIndex = sourceCombo.getSelectionIndex();
                Object selectedItem = null;
                if (selectionIndex != -1) {
                    selectedItem = sourceCombo.getItem(selectionIndex);
                }

                sourceCombo.deselectAll();
                List<String> newItems = new ArrayList<>();
                newItems.add(ANY_SOURCE);
                List<String> sortedSources = new ArrayList<>(eventSources);
                Collections.sort(sortedSources, String.CASE_INSENSITIVE_ORDER);
                newItems.addAll(sortedSources);

                sourceCombo.setItems(newItems.toArray(new String[0]));

                if (selectedItem != null) {
                    sourceCombo.select(newItems.indexOf(selectedItem));
                } else {
                    sourceCombo.select(0);
                }
            }

            String filterText = sourceCombo.getText();
            return filterText.equals(ANY_SOURCE) || filterText.equals(event.getSource());
        }
        return false;
    }
}
