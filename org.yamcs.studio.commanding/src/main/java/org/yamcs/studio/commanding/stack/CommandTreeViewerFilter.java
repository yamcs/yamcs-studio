package org.yamcs.studio.commanding.stack;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.ui.XtceSubSystemNode;

/**
 * JFace ViewerFilter for use with GPB CommandInfo
 */
public class CommandTreeViewerFilter extends ViewerFilter {

    private String regex = ".*";
    private CommandTreeContentProvider contentProvider;

    public CommandTreeViewerFilter(CommandTreeContentProvider contentProvider) {
        super();
        this.contentProvider = contentProvider;
    }

    public void setSearchTerm(String searchTerm) {
        regex = "(?i:.*" + searchTerm + ".*)";
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        return elementMatches(element);
    }

    private boolean elementMatches(Object element) {
        if (element instanceof XtceCommandNode) {
            var cmd = ((XtceCommandNode) element).getCommandInfo();
            for (NamedObjectId alias : cmd.getAliasList()) {
                if (alias.getName().matches(regex)) {
                    return true;
                }
            }
            return cmd.getQualifiedName().matches(regex);
        } else if (element instanceof XtceSubSystemNode) {
            var children = contentProvider.getChildren(element);
            for (Object child : children) {
                if (elementMatches(child)) {
                    return true;
                }
            }
        }
        return false;
    }
}
