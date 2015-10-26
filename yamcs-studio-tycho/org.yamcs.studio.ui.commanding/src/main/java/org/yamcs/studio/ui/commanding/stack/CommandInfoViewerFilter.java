package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.yamcs.protobuf.Mdb.CommandInfo;

/**
 * JFace ViewerFilter for use with GPB CommandInfo
 */
public class CommandInfoViewerFilter extends ViewerFilter {

    private String regex = ".*";

    public void setSearchTerm(String searchTerm) {
        regex = "(?i:.*" + searchTerm + ".*)";
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        CommandInfo cmd = (CommandInfo) element;
        return cmd.getQualifiedName().matches(regex);
    }
}
