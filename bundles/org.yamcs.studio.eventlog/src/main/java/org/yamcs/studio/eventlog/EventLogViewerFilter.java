package org.yamcs.studio.eventlog;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogViewerFilter extends ViewerFilter {

    private String regex = ".*";

    public void setSearchTerm(String searchTerm) {
        regex = "(?i:.*" + searchTerm + ".*)";
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        return elementMatches(element);
    }

    private boolean elementMatches(Object element) {
        if (element instanceof Event) {
            Event event = (Event) element;
            if (event.getMessage().matches(regex)) {
                return true;
            }
        }
        return false;
    }
}
