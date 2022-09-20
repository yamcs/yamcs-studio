/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.properties;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.FilePathPropertyDescriptor;
import org.csstudio.opibuilder.script.RuleData;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

/**
 * The property for file path, which is represented as a String.
 */
public class FilePathProperty extends AbstractWidgetProperty<String> {

    /**
     * The file extension, which should be accepted.
     */
    private String[] fileExtensions;

    private boolean buildAbsolutePath;

    /**
     * File Path Property Constructor. The property value type is {@link IPath}. It will automatically build the
     * absolute path if it is relative path.
     * 
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created.
     * @param fileExtensions
     *            the allowed file extensions in the file open dialog.
     */
    public FilePathProperty(String prop_id, String description, WidgetPropertyCategory category, String defaultValue,
            String[] fileExtensions) {
        this(prop_id, description, category, defaultValue, fileExtensions, true);
    }

    /**
     * File Path Property Constructor. The property value type is {@link IPath}.
     * 
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created.
     * @param fileExtensions
     *            the allowed file extensions in the file open dialog.
     * @param buildAbsolutePath
     *            true if it should automatically build the absolute path from widget model.
     */
    public FilePathProperty(String prop_id, String description, WidgetPropertyCategory category, String defaultValue,
            String[] fileExtensions, boolean buildAbsolutePath) {
        super(prop_id, description, category, defaultValue == null ? "" : defaultValue);
        this.fileExtensions = fileExtensions;
        this.buildAbsolutePath = buildAbsolutePath;
    }

    @Override
    public String checkValue(Object value) {
        if (value == null) {
            return null;
        }
        String acceptedValue = null;

        if (value instanceof IPath || value instanceof String) {
            String path;
            if (value instanceof String) {
                path = (String) value;
            } else {
                path = ((IPath) value).toPortableString();
            }
            var idx = path.lastIndexOf('.');
            var dotExt = (idx != -1) ? path.substring(idx) : "";
            if (fileExtensions != null && fileExtensions.length > 0) {
                for (var extension : fileExtensions) {
                    if (("." + extension).equalsIgnoreCase(dotExt)) {
                        acceptedValue = path;
                    }
                    if (extension.equals("*")) {
                        acceptedValue = path;
                    }
                }
            } else {
                acceptedValue = path;
            }
            if (path.isEmpty()) {
                acceptedValue = path;
            }
        }

        return acceptedValue;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new FilePathPropertyDescriptor(prop_id, description, widgetModel, fileExtensions);
    }

    @Override
    public String getPropertyValue() {
        if (widgetModel != null && widgetModel.getExecutionMode() == ExecutionMode.RUN_MODE && propertyValue != null
                && !propertyValue.isEmpty()) {
            var s = OPIBuilderMacroUtil.replaceMacros(widgetModel, propertyValue);
            if (s.contains("://")) {
                return s;
            } else {
                var path = ResourceUtil.getPathFromString(s);
                if (buildAbsolutePath && !path.isAbsolute()) {
                    path = ResourceUtil.buildAbsolutePath(widgetModel, path);
                }
                return path.toPortableString();
            }
        }
        return super.getPropertyValue();
    }

    @Override
    public String readValueFromXML(Element propElement) {
        return propElement.getText();
    }

    @Override
    public void writeToXML(Element propElement) {
        var value = getPropertyValue();
        if (value.contains("://")) {
            propElement.setText(value);
        } else {
            var path = ResourceUtil.getPathFromString(value);
            propElement.setText(path.toPortableString());
        }
    }

    @Override
    public boolean configurableByRule() {
        return true;
    }

    @Override
    public String toStringInRuleScript(String propValue) {
        return RuleData.QUOTE + super.toStringInRuleScript(propValue) + RuleData.QUOTE;
    }
}
