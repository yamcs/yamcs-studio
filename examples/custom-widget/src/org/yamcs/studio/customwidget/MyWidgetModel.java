package org.yamcs.studio.customwidget;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.DoubleProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.eclipse.swt.graphics.RGB;

public class MyWidgetModel extends AbstractPVWidgetModel {

    public static final String PROP_MIN = "max";
    public static final String PROP_MAX = "min";

    public MyWidgetModel() {
        setForegroundColor(new RGB(255, 0, 0));
        setBackgroundColor(new RGB(0, 0, 255));
        setSize(50, 100);
    }

    @Override
    public String getTypeID() {
        return "org.yamcs.studio.customwidget.MyWidget";
    }

    @Override
    protected void configureProperties() {
        addProperty(new DoubleProperty(PROP_MIN, "Min", WidgetPropertyCategory.Behavior, 0));
        addProperty(new DoubleProperty(PROP_MAX, "Max", WidgetPropertyCategory.Behavior, 100));
    }

    public double getMin() {
        return getCastedPropertyValue(PROP_MIN);
    }

    public double getMax() {
        return getCastedPropertyValue(PROP_MAX);
    }
}
