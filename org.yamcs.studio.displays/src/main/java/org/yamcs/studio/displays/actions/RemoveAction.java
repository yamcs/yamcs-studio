package org.yamcs.studio.displays.actions;

import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.yamcs.studio.displays.ParameterTableViewer;

public class RemoveAction extends Action {

    private ParameterTableViewer viewer;

    public RemoveAction(ParameterTableViewer viewer) {
        this.viewer = viewer;
        setToolTipText("Remove");
        setImageDescriptor(getImageDescriptor("icons/elcl16/remove.png"));

        setEnabled(false);
        ISelectionChangedListener listener = event -> setEnabled(!event.getSelection().isEmpty());
        viewer.addSelectionChangedListener(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        List<String> selected = viewer.getStructuredSelection().toList();
        for (String info : selected) {
            viewer.removeParameter(info);
        }
    }

    private ImageDescriptor getImageDescriptor(String path) {
        return ImageDescriptor
                .createFromURL(FileLocator.find(Platform.getBundle("org.yamcs.studio.displays"), new Path(path), null));
    }
}
