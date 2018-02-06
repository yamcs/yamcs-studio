package org.yamcs.studio.ui.alphanum.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.yamcs.studio.ui.alphanum.ParameterTableViewer;

public abstract class AlphaNumericAction extends Action {

    protected ParameterTableViewer viewer;

    public AlphaNumericAction(String icon, final ParameterTableViewer viewer) {
        this.viewer = viewer;
        setImageDescriptor(getImageDescriptor(icon));

    }

    public void setViewer(ParameterTableViewer viewer) {
        this.viewer = viewer;
    }

    public ParameterTableViewer getViewer() {
        return viewer;
    }

    private ImageDescriptor getImageDescriptor(String path) {
        return ImageDescriptor.createFromURL(FileLocator
                .find(Platform.getBundle("org.yamcs.studio.ui.alphanum"),
                        new Path(path),null));


    }
}
