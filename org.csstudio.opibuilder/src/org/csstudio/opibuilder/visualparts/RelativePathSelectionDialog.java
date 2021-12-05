/********************************************************************************
 * Copyright (c) 2007 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.visualparts;

import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.ui.util.composites.ResourceSelectionGroup;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class represents a Dialog to choose a file in the workspace. There is an option to return relative path.
 */
public final class RelativePathSelectionDialog extends Dialog implements Listener {
    /**
     * The message to display, or <code>null</code> if none.
     */
    private String _message;

    /**
     * The {@link WorkspaceTreeComposite} for this dialog.
     */
    private ResourceSelectionGroup _resourceSelectionGroup;

    /**
     * The file extensions of files that will be shown for selection.
     */
    private String[] _fileExtensions;

    /**
     * The path of the selected resource.
     */
    private String _path;

    private IPath refPath;

    private Text _resourcePathText;

    private boolean relative;

    /**
     * Creates an input dialog with OK and Cancel buttons. Note that the dialog will have no visual representation (no
     * widgets) until it is told to open.
     * <p>
     * Note that the <code>open</code> method blocks for input dialogs.
     * </p>
     *
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level shell
     * @param refPath
     *            the reference path which doesn't include the file name.
     * @param dialogMessage
     *            the dialog message, or <code>null</code> if none
     * @param fileExtensions
     *            the file extensions of files to show in the dialog. Use an empty array or <code>null</code> to show
     *            only containers (folders).
     */
    public RelativePathSelectionDialog(Shell parentShell, IPath refPath, String dialogMessage,
            String[] fileExtensions) {
        super(parentShell);
        this.setShellStyle(SWT.MODELESS | SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER | SWT.RESIZE);
        _message = dialogMessage;
        this.refPath = refPath;
        relative = true;
        _fileExtensions = fileExtensions;
    }

    /**
     * Sets the initially selected resource. Must be called before the dialog is displayed.
     *
     * @param path
     *            the path to the initially selected resource.
     */
    public void setSelectedResource(String path) {
        _path = path;
        relative = path.contains("://") || !Path.fromPortableString(path).isAbsolute();
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Resources");
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        if (_message != null) {
            Label label = new Label(composite, SWT.WRAP);
            label.setText(_message);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_CENTER);
            data.horizontalSpan = 2;
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            label.setLayoutData(data);
        }

        // The New Project and New Folder actions will be shown if there are
        // no file extensions, i.e. if the dialog is opened to select a folder.
        boolean showNewContainerActions = (_fileExtensions == null
                || _fileExtensions.length == 0);

        _resourceSelectionGroup = new ResourceSelectionGroup(composite, this,
                _fileExtensions, showNewContainerActions);
        new Label(composite, SWT.NONE).setText("Resource Path:");
        _resourcePathText = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        _resourcePathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (_path != null && !_path.isEmpty()) {
            _resourcePathText.setText(_path.toString());
            if (!_path.contains("://")) {
                IPath relPath = Path.fromPortableString(_path);
                if (relative) {
                    _resourceSelectionGroup.setSelectedResource(refPath.append(relPath));
                } else {
                    _resourceSelectionGroup.setSelectedResource(relPath);
                }
            }
        }
        // the check box for relative path
        Button checkBox = new Button(composite, SWT.CHECK);
        checkBox.setSelection(relative);
        checkBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        checkBox.setText("Return relative path");
        checkBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                relative = checkBox.getSelection();
                if (relative && !_path.contains("://")) {
                    IPath relPath = Path.fromPortableString(_path);
                    _resourcePathText.setText(ResourceUtil.buildRelativePath(refPath, relPath).toString());
                } else {
                    _resourcePathText.setText(_path);
                }
            }
        });

        return composite;
    }

    @Override
    protected void okPressed() {
        _path = _resourcePathText.getText();
        super.okPressed();
    }

    /**
     * Returns the path to the selected resource.
     *
     * @return the path to the selected resource, or <code>null</code> if no resource was selected.
     */
    public String getSelectedResource() {
        return _path;
    }

    @Override
    public void handleEvent(Event event) {
        ResourceSelectionGroup widget = (ResourceSelectionGroup) event.widget;

        IPath fullPath = widget.getFullPath();
        _path = (fullPath == null) ? null : fullPath.toPortableString();
        if (_path == null) {
            return;
        }
        if (relative) {
            _resourcePathText.setText(ResourceUtil.buildRelativePath(refPath, fullPath).toString());
        } else {
            _resourcePathText.setText(fullPath.toString());
        }

        if (event.type == SWT.MouseDoubleClick) {
            okPressed();
        }
    }
}
