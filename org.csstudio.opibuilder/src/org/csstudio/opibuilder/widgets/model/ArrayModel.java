/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import java.util.List;

import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.IPVWidgetModel;
import org.csstudio.opibuilder.model.PVWidgetModelDelegate;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.Version;

/**
 * The model for array widget.
 */
public class ArrayModel extends AbstractContainerModel implements IPVWidgetModel {

    public enum ArrayDataType {
        DOUBLE_ARRAY("double[]"),
        STRING_ARRAY("String[]"),
        INT_ARRAY("int[]"),
        BYTE_ARRAY("byte[]"),
        LONG_ARRAY("long[]"),
        SHORT_ARRAY("short[]"),
        FLOAT_ARRAY("float[]"),
        OBJECT_ARRAY("Object[]");

        private String description;

        private ArrayDataType(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        public static String[] stringValues() {
            var result = new String[values().length];
            var i = 0;
            for (var f : values()) {
                result[i++] = f.toString();
            }
            return result;
        }
    }

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.array";

    /**
     * The key to get unique propId info from other widgets.
     */
    public static final String ARRAY_UNIQUEPROP_ID = "array.uniquePropId.List";

    /**
     * Array Length
     */
    public static final String PROP_ARRAY_LENGTH = "array_length";

    /**
     * If the array widget is layoutted in horizontal.
     */
    public static final String PROP_HORIZONTAL = "horizontal";

    public static final String PROP_SHOW_SPINNER = "show_spinner";

    public static final String PROP_SHOW_SCROLLBAR = "show_scrollbar";

    public static final String PROP_SPINNER_WIDTH = "spinner_width";

    public static final String PROP_VISIBLE_ELEMENTS_COUNT = "vec";

    public static final String PROP_DATA_TYPE = "data_type";

    private PVWidgetModelDelegate delegate;

    public ArrayModel() {
        setSize(150, 122);
        setForegroundColor(new RGB(0, 0, 0));
    }

    public PVWidgetModelDelegate getDelegate() {
        if (delegate == null) {
            delegate = new PVWidgetModelDelegate(this);
        }
        return delegate;
    }

    @Override
    public synchronized void addChild(AbstractWidgetModel child, boolean changeParent) {
        if (!getChildren().isEmpty()) {
            return;
        }
        // child should not be scalable because their size are layoutted by the array figure.
        child.setScaleOptions(false, false, false);
        super.addChild(child, changeParent);
        for (var i = 1; i < getVisibleElementsCount(); i++) {
            try {
                var clone = XMLUtil.XMLElementToWidget(XMLUtil.widgetToXMLElement(child));
                super.addChild(clone, changeParent);
            } catch (Exception e) {
                ErrorHandlerUtil.handleError("Failed to generate copy of the element widget in array widget.", e);
            }
        }
    }

    @Override
    public synchronized void addChild(int index, AbstractWidgetModel child) {
        addChild(child, true);
    }

    @Override
    public synchronized void removeChild(AbstractWidgetModel child) {
        removeAllChildren();
    }

    @Override
    protected void configureBaseProperties() {
        super.configureBaseProperties();
        getDelegate().configureBaseProperties();
    }

    @Override
    protected void configureProperties() {
        addProperty(new IntegerProperty(PROP_ARRAY_LENGTH, "Array Length", WidgetPropertyCategory.Behavior, 10, 0,
                Integer.MAX_VALUE));
        addProperty(
                new IntegerProperty(PROP_SPINNER_WIDTH, "Spinner Width", WidgetPropertyCategory.Display, 40, 0, 1000));
        addProperty(new BooleanProperty(PROP_HORIZONTAL, "Horizontal", WidgetPropertyCategory.Display, false));
        addProperty(new BooleanProperty(PROP_SHOW_SPINNER, "Show Spinner", WidgetPropertyCategory.Display, true));
        addProperty(new BooleanProperty(PROP_SHOW_SCROLLBAR, "Show Scrollbar", WidgetPropertyCategory.Display, true));
        addProperty(new IntegerProperty(PROP_VISIBLE_ELEMENTS_COUNT, "Visible Elements Count",
                WidgetPropertyCategory.Display, 1, 0, 1000));
        addProperty(new ComboProperty(PROP_DATA_TYPE, "Data Type", WidgetPropertyCategory.Behavior,
                ArrayDataType.stringValues(), 0));

        setPropertyVisibleAndSavable(PROP_VISIBLE_ELEMENTS_COUNT, false, true);
        getProperty(PROP_VISIBLE_ELEMENTS_COUNT).addPropertyChangeListener(evt -> {
            if (getChildren().size() < 1) {
                return;
            }
            var child = getChildren().get(0);
            removeAllChildren();
            addChild(child);
        });
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    public int getArrayLength() {
        return (Integer) getPropertyValue(PROP_ARRAY_LENGTH);
    }

    public int getSpinnerWidth() {
        return (Integer) getPropertyValue(PROP_SPINNER_WIDTH);
    }

    public int getVisibleElementsCount() {
        return (Integer) getPropertyValue(PROP_VISIBLE_ELEMENTS_COUNT);
    }

    public boolean isHorizontal() {
        return (Boolean) getPropertyValue(PROP_HORIZONTAL);
    }

    public boolean isShowSpinner() {
        return (Boolean) getPropertyValue(PROP_SHOW_SPINNER);
    }

    public Boolean isShowScrollbar() {
        return (Boolean) getPropertyValue(PROP_SHOW_SCROLLBAR);
    }

    public ArrayDataType getDataType() {
        return ArrayDataType.values()[(Integer) getPropertyValue(PROP_DATA_TYPE)];
    }

    @Override
    public void processVersionDifference(Version boyVersionOnFile) {
        super.processVersionDifference(boyVersionOnFile);
        delegate.processVersionDifference(boyVersionOnFile);
    }

    @Override
    public List<AbstractWidgetModel> getChildren() {
        if (super.getChildren().size() > 1) {
            return super.getChildren().subList(0, 1);
        }
        return super.getChildren();
    }

    public List<AbstractWidgetModel> getAllChildren() {
        return super.getChildren();
    }

    @Override
    public boolean isChildrenOperationAllowable() {
        return false;
    }

    @Override
    public boolean isBorderAlarmSensitve() {
        return getDelegate().isBorderAlarmSensitve();
    }

    @Override
    public boolean isForeColorAlarmSensitve() {
        return getDelegate().isForeColorAlarmSensitve();
    }

    @Override
    public boolean isBackColorAlarmSensitve() {
        return getDelegate().isBackColorAlarmSensitve();
    }

    @Override
    public String getPVName() {
        return getDelegate().getPVName();
    }

    @Override
    public boolean isAlarmPulsing() {
        return getDelegate().isAlarmPulsing();
    }

    public void setArrayLength(int length) {
        getProperty(PROP_ARRAY_LENGTH).setPropertyValue(length);
    }

    public void setDataType(ArrayDataType type) {
        getProperty(PROP_DATA_TYPE).setPropertyValue(type.ordinal());
    }

    @Override
    public void scaleChildren() {
        // if(!getChildren().isEmpty() && getChildren().get(0) instanceof AbstractContainerModel)
        // for(AbstractWidgetModel child : getAllChildren()){
        // ((AbstractContainerModel) child).scaleChildren();
        // }
    }
}
