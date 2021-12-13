/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A custom cell dialog editor to browse file path applying filters.
 */
public final class FilePathCellDialogEditorWithFilter extends AbstractDialogCellEditor {

    /**
     * The default value for the file extensions.
     */
    private static final String[] IMAGE_EXTENSIONS = new String[] { "gif", "GIF", "png", "PNG", "svg", "SVG" };

    /**
     * The regular expression for TTT pattern like FFF-FFF-FFF:TTT1234-AAAA TODO store it in INI file
     */
    private static String TTT_REGEX;

    /**
     * The current IPath.
     */
    private IPath path;

    /**
     * The filter path for the dialog.
     */
    @SuppressWarnings("unused")
    private String filterPath = System.getProperty("user.home");

    /**
     * The accepted file extensions.
     */
    private String[] filters;
    /**
     * The original file extensions.
     */
    private String[] orgFileExtensions;

    /**
     * TODO only use temporarily.
     */
    private boolean onlyWorkSpace = true;

    private AbstractWidgetModel widgetModel;

    public FilePathCellDialogEditorWithFilter(Composite parent, AbstractWidgetModel widgetModel,
            String[] fileExtensions) {
        super(parent, "Open File");
        orgFileExtensions = fileExtensions;
        this.widgetModel = widgetModel;
        convertFileExtensions();
        var service = Platform.getPreferencesService();
        TTT_REGEX = service.getString("org.csstudio.opibuilder.widgets.symbol", "filter_regex", "", null);
    }

    /**
     * Convert the file extensions. Add '*.' to each extension if it does not start with it.
     */
    private void convertFileExtensions() {
        if (onlyWorkSpace) {
            filters = orgFileExtensions;
        } else {
            if (orgFileExtensions.length > 0) {
                filters = new String[orgFileExtensions.length];
                for (var i = 0; i < filters.length; i++) {
                    if (orgFileExtensions[i].startsWith("*.")) {
                        filters[i] = orgFileExtensions[i];
                    } else {
                        filters[i] = "*." + orgFileExtensions[i];
                    }
                }
            }
        }
    }

    @Override
    protected Object doGetValue() {
        return path;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof IPath)) {
            path = new Path("");
        } else {
            path = (IPath) value;
        }
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        if (onlyWorkSpace) {
            var pvName = (String) widgetModel.getPropertyValue(AbstractPVWidgetModel.PROP_PVNAME);
            if (!pvName.isEmpty()) {
                var listToFind = new ArrayList<String>();
                var pattern = Pattern.compile(TTT_REGEX);
                var matcher = pattern.matcher(pvName);
                if (matcher.find()) {
                    for (var i = 0; i <= matcher.groupCount(); i++) {
                        listToFind.add(matcher.group(i));
                    }
                }
                if (!listToFind.isEmpty()) {
                    filters = listToFind.toArray(new String[listToFind.size()]);
                }
            } else {
                filters = IMAGE_EXTENSIONS;
            }

            var rsd = new FilePathDialogWithFilter(parentShell,
                    widgetModel.getRootDisplayModel().getOpiFilePath().removeLastSegments(1), "Select a resource",
                    filters);
            rsd.setSelectedResource(path);
            if (rsd.open() == Window.OK) {
                if (rsd.getSelectedResource() != null) {
                    path = rsd.getSelectedResource();
                }
            }
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return path != null;
    }
}
