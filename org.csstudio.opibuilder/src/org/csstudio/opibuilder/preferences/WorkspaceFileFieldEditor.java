/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.preferences;

import org.csstudio.ui.util.dialogs.ResourceSelectionDialog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A field editor for a workspace file path type preference. A workspace file dialog appears when the user presses the
 * change button.
 */
public class WorkspaceFileFieldEditor extends StringButtonFieldEditor {

    private String[] extensions = null;

    /**
     * Creates a new file field editor
     */
    protected WorkspaceFileFieldEditor() {
    }

    /**
     * Creates a file field editor.
     *
     * @param name
     *            the name of the preference this field editor works on
     * @param labelText
     *            the label text of the field editor
     * @param parent
     *            the parent of the field editor's control
     */
    public WorkspaceFileFieldEditor(String name, String labelText, Composite parent) {
        this(name, labelText, new String[] { "*" }, parent);
    }

    /**
     * Creates a file field editor.
     *
     * @param name
     *            the name of the preference this field editor works on
     * @param labelText
     *            the label text of the field editor
     * @param extensions
     *            the file extensions
     * @param parent
     *            the parent of the field editor's control
     */
    public WorkspaceFileFieldEditor(String name, String labelText, String[] extensions, Composite parent) {
        super(name, labelText, parent);
        setFileExtensions(extensions);
        setChangeButtonText("Browse...");
    }

    @Override
    protected String changePressed() {
        IPath startPath = new Path(getTextControl().getText());
        var path = getPath(startPath);
        if (path != null) {
            return path.toPortableString();
        } else {
            return null;
        }

    }

    private IPath getPath(IPath startPath) {
        var rsDialog = new ResourceSelectionDialog(Display.getCurrent().getActiveShell(), "Choose File", extensions);
        if (startPath != null) {
            rsDialog.setSelectedResource(startPath);
        }

        if (rsDialog.open() == Window.OK) {
            return rsDialog.getSelectedResource();
        }
        return null;
    }

    @Override
    protected boolean checkState() {
        return true;

    }

    /**
     * Sets this file field editor's file extension filter.
     *
     * @param extensions
     *            a list of file extension, or <code>null</code> to set the filter to the system's default value
     */
    public void setFileExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    public void setTooltip(String tooltip) {
        getLabelControl().setToolTipText(tooltip);
        getTextControl().setToolTipText(tooltip);
        getChangeControl(getTextControl().getParent()).setToolTipText(tooltip);
    }
}
