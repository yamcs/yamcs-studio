package org.yamcs.studio.alphanumeric.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.yamcs.studio.alphanumeric.ParameterTableViewer;
import org.yamcs.studio.alphanumeric.ScrollParameterTableViewer;

public abstract class AlphaNumericAction extends Action {

    protected ParameterTableViewer viewer;
    protected ScrollParameterTableViewer scrollViewer;

    public AlphaNumericAction(String icon, final ParameterTableViewer viewer) {
        this.viewer = viewer;
        setImageDescriptor(getImageDescriptor(icon));

    }

    public AlphaNumericAction(String icon, final ScrollParameterTableViewer viewer) {
        this.scrollViewer = viewer;
        setImageDescriptor(getImageDescriptor(icon));

    }


    public void setViewer(ParameterTableViewer viewer) {
        this.viewer = viewer;
    }

    public ParameterTableViewer getViewer() {
        return viewer;
    }

    public void setScrollViewer(ScrollParameterTableViewer viewer) {
        this.scrollViewer = viewer;
    }

    public ScrollParameterTableViewer getScrollViewer() {
        return scrollViewer;
    }

    private ImageDescriptor getImageDescriptor(String path) {
        return ImageDescriptor.createFromURL(FileLocator
                .find(Platform.getBundle("org.yamcs.studio.alphanumeric"),
                        new Path(path),null));


    }
}