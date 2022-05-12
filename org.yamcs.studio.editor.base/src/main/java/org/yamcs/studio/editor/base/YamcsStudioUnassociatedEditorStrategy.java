package org.yamcs.studio.editor.base;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ide.IUnassociatedEditorStrategy;

public class YamcsStudioUnassociatedEditorStrategy implements IUnassociatedEditorStrategy {

    public static final String GENERIC_EDITOR_ID = "org.eclipse.ui.genericeditor.GenericEditor";

    @Override
    public IEditorDescriptor getEditorDescriptor(String fileName, IEditorRegistry editorRegistry)
            throws CoreException, OperationCanceledException {
        return editorRegistry.findEditor(GENERIC_EDITOR_ID);
    }
}
