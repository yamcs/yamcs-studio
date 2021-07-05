package org.csstudio.opibuilder.editor;

import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

public class SchemaDecorator implements ILightweightLabelDecorator {

    public static final String ID = "org.csstudio.opibuilder.SchemaDecorator";

    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof IFile) {
            IFile file = (IFile) element;
            if ("opi".equalsIgnoreCase(file.getFileExtension())) {
                IPath schemaPath = PreferencesHelper.getSchemaOPIPath();
                if (schemaPath != null) {
                    IFile schemaFile = ResourceUtil.getIFileFromIPath(schemaPath);
                    if (schemaFile.equals(file)) {
                        ImageDescriptor descriptor = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                                "icons/ovr16/annotation_tsk.png");
                        decoration.addOverlay(descriptor, IDecoration.TOP_RIGHT);
                        decoration.addSuffix(" [schema]");
                    }
                }
            }
        }
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }
}
