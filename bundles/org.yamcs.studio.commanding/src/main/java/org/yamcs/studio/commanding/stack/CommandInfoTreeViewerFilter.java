package org.yamcs.studio.commanding.stack;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.commanding.stack.AddToStackWizardPage1.CommandTreeContentProvider;

/**
 * JFace ViewerFilter for use with GPB CommandInfo
 */
public class CommandInfoTreeViewerFilter extends ViewerFilter {

    private String regex = ".*";
    private CommandTreeContentProvider commandTreeContentProvider;

    CommandInfoTreeViewerFilter(CommandTreeContentProvider commandTreeContentProvider) {
        super();
        this.commandTreeContentProvider = commandTreeContentProvider;
    }

    public void setSearchTerm(String searchTerm) {
        regex = "(?i:.*" + searchTerm + ".*)";
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        CommandInfo cmd = (CommandInfo) element;
        return matching(cmd) || childrenMatching(cmd);
    }

    private boolean matching(CommandInfo cmd) {
        // check match in all namespaces
        boolean matching = false;
        for (NamedObjectId alias : cmd.getAliasList()) {
            matching |= alias.getName().matches(regex);
        }
        matching |= cmd.getQualifiedName().matches(regex);
        return matching;
    }

    private boolean childrenMatching(CommandInfo cmd) {
        Object[] children = commandTreeContentProvider.getChildren(cmd);
        boolean matching = false;
        for (Object ci : children) {
            matching = matching((CommandInfo) ci) || childrenMatching((CommandInfo) ci);
            if (matching) // return as soon as an element matches
                return true;
        }
        return false;
    }

}
