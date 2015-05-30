package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.yamcs.xtce.MetaCommand;

/**
 * JFace ViewerFilter for use with XTCE MetaCommands
 */
public class MetaCommandViewerFilter extends ViewerFilter {

    private String regex = ".*";

    public void setSearchTerm(String searchTerm) {
        regex = "(?i:.*" + searchTerm + ".*)";
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        MetaCommand cmd = (MetaCommand) element;
        return cmd.getOpsName().matches(regex);
    }
}
