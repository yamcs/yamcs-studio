/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.StringTablePropertyDescriptor;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.csstudio.ui.util.swt.stringtable.StringTableEditor.CellEditorType;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom2.Element;

/**
 * The property for string table.
 */
public class StringTableProperty extends AbstractWidgetProperty<String[][]> {

    public class TitlesProvider {

        public String[] getTitles() {
            return titles;
        }
    }

    /**
     * XML ELEMENT name for a row.
     */
    public static final String XML_ELEMENT_ROW = "row";

    /**
     * XML ELEMENT name for a column.
     */
    public static final String XML_ELEMENT_COLUMN = "col";

    private String[] titles;

    private TitlesProvider titlesProvider;

    private CellEditorType[] cellEditorTypes;

    private Object[] cellEditorDatas;

    /**
     * StringList Property Constructor. The property value type is 2D string array.
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created. It cannot be null.
     * @param titles
     *            the title for each column. The length of titles array is the number of columns. it can be null if the
     *            property is not visible.
     */
    public StringTableProperty(String prop_id, String description, WidgetPropertyCategory category,
            String[][] defaultValue, String[] titles) {
        this(prop_id, description, category, defaultValue, titles, null, null);
    }

    /**
     * StringList Property Constructor. The property value type is 2D string array.
     *
     * @param prop_id
     *            the property id which should be unique in a widget model.
     * @param description
     *            the description of the property, which will be shown as the property name in property sheet.
     * @param category
     *            the category of the widget.
     * @param defaultValue
     *            the default value when the widget is first created. It cannot be null.
     * @param titles
     *            the title for each column. The length of titles array is the number of columns. it can be null if the
     *            property is not visible.
     */
    public StringTableProperty(String prop_id, String description, WidgetPropertyCategory category,
            String[][] defaultValue, String[] titles, CellEditorType[] cellEditorTypes, Object[] cellEditorDatas) {
        super(prop_id, description, category, defaultValue);
        this.titles = titles;
        this.cellEditorTypes = cellEditorTypes;
        this.cellEditorDatas = cellEditorDatas;
        titlesProvider = new TitlesProvider();
    }

    @Override
    public String[][] checkValue(Object value) {
        if (value == null) {
            return null;
        }
        String[][] acceptableValue = null;
        if (value instanceof String[][]) {
            acceptableValue = (String[][]) value;
        }
        return acceptableValue;
    }

    @Override
    public String[][] getPropertyValue() {
        if (widgetModel != null && widgetModel.getExecutionMode() == ExecutionMode.RUN_MODE) {
            var originValue = super.getPropertyValue();
            if (originValue.length <= 0) {
                return originValue;
            }
            var result = new String[originValue.length][originValue[0].length];
            for (var i = 0; i < originValue.length; i++) {
                for (var j = 0; j < originValue[0].length; j++) {
                    result[i][j] = OPIBuilderMacroUtil.replaceMacros(widgetModel, originValue[i][j]);
                }
            }
            return result;
        } else {
            return super.getPropertyValue();
        }
    }

    /**
     * @param titles
     *            the titles for each column.
     */
    public void setTitles(String[] titles) {
        this.titles = titles;
    }

    @Override
    protected PropertyDescriptor createPropertyDescriptor() {
        return new StringTablePropertyDescriptor(prop_id, description, titlesProvider, cellEditorTypes,
                cellEditorDatas);
    }

    @Override
    public String[][] readValueFromXML(Element propElement) {
        var rowChildren = propElement.getChildren();
        if (rowChildren.size() == 0) {
            return new String[0][0];
        }
        var result = new String[rowChildren.size()][((Element) rowChildren.get(0)).getChildren().size()];
        int i = 0, j = 0;
        for (var oe : rowChildren) {
            var re = (Element) oe;
            if (re.getName().equals(XML_ELEMENT_ROW)) {
                j = 0;
                for (var oc : re.getChildren()) {
                    result[i][j++] = ((Element) oc).getText();
                }
                i++;
            }
        }
        return result;
    }

    @Override
    public void writeToXML(Element propElement) {
        for (var row : propertyValue) {
            var rowElement = new Element(XML_ELEMENT_ROW);
            for (var e : row) {
                var colElement = new Element(XML_ELEMENT_COLUMN);
                colElement.setText(e);
                rowElement.addContent(colElement);
            }
            propElement.addContent(rowElement);
        }
    }
}
