package org.yamcs.studio.eventlog;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogSearchBoxFilter extends ViewerFilter {

    private Pattern pattern = Pattern.compile(".*");

    public void setSearchTerm(String searchTerm) {
        pattern = Pattern.compile("(?i:.*" + searchTerm + ".*)");
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof EventLogItem) {
            Event event = ((EventLogItem) element).event;
            if (pattern.matcher(event.getMessage()).matches()
                    || (event.hasType() && pattern.matcher(event.getType()).matches())
                    || (event.hasSource() && pattern.matcher(event.getSource()).matches())) {
                return true;
            }
        }
        return false;
    }
}
