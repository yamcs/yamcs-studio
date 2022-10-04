/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public final class FilePathCellEditor extends AbstractDialogCellEditor {

    /**
     * The current IPath.
     */
    private String _path;

    /**
     * The filter path for the dialog.
     */
    private String _filterPath = System.getProperty("user.home");

    /**
     * The accepted file extensions.
     */
    private String[] _fileExtensions;
    /**
     * The original file extensions.
     */
    private String[] _orgFileExtensions;

    /**
     * TODO only use temporarily.
     */
    private boolean _onlyWorkSpace = true;

    private AbstractWidgetModel widgetModel;

    /**
     * Creates a new string cell editor parented under the given control. The cell editor value is a PointList.
     *
     * @param parent
     *            The parent table.
     * @param widgetModel
     *            the reference path which doesn't include the file name.
     * @param fileExtensions
     *            The accepted file extensions
     */
    public FilePathCellEditor(Composite parent, AbstractWidgetModel widgetModel, String[] fileExtensions) {
        super(parent, "Open File");
        _orgFileExtensions = fileExtensions;
        this.widgetModel = widgetModel;
        convertFileExtensions();
    }

    /**
     * Converts the file extensions. Adds '*.' to every extension if it doesn't start with it
     */
    private void convertFileExtensions() {
        if (_onlyWorkSpace) {
            _fileExtensions = _orgFileExtensions;
        } else {
            if (_orgFileExtensions.length > 0) {
                _fileExtensions = new String[_orgFileExtensions.length];
                for (var i = 0; i < _fileExtensions.length; i++) {
                    if (_orgFileExtensions[i].startsWith("*.")) {
                        _fileExtensions[i] = _orgFileExtensions[i];
                    } else {
                        _fileExtensions[i] = "*." + _orgFileExtensions[i];
                    }
                }
            }
        }
    }

    @Override
    protected Object doGetValue() {
        return _path;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof String)) {
            _path = "";
        } else {
            _path = (String) value;
        }
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        if (_onlyWorkSpace) {
            var rsd = new RelativePathSelectionDialog(parentShell,
                    widgetModel.getRootDisplayModel().getOpiFilePath().removeLastSegments(1), "Select a resource",
                    _fileExtensions);
            if (_path != null && !_path.isEmpty()) {
                rsd.setSelectedResource(_path);
            } else {
                // select current path
                rsd.setSelectedResource("./");
            }

            if (rsd.open() == Window.OK) {
                if (rsd.getSelectedResource() != null) {
                    _path = rsd.getSelectedResource();
                }
            }
        } else {
            var dialog = new FileDialog(parentShell, SWT.OPEN | SWT.MULTI);
            dialog.setText(dialogTitle);
            if (_path != null) {
                _filterPath = _path.toString();
            }
            dialog.setFilterPath(_filterPath);
            dialog.setFilterExtensions(_fileExtensions);
            dialog.open();
            var name = dialog.getFileName();
            _filterPath = dialog.getFilterPath();
            _path = new Path(_filterPath + Path.SEPARATOR + name).toPortableString();
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return _path != null;
    }
}
