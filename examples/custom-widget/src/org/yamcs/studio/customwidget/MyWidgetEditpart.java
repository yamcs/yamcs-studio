package org.yamcs.studio.customwidget;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.simplepv.VTypeHelper;
import org.diirt.vtype.VType;
import org.eclipse.draw2d.IFigure;

public class MyWidgetEditpart extends AbstractPVWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        MyWidgetFigure figure = new MyWidgetFigure();
        figure.setMin(getWidgetModel().getMin());
        figure.setMax(getWidgetModel().getMax());
        return figure;
    }

    @Override
    public MyWidgetModel getWidgetModel() {
        return (MyWidgetModel) super.getWidgetModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVVALUE, (oldValue, newValue, figure) -> {
            if (newValue == null) {
                return false;
            }
            ((MyWidgetFigure) figure).setValue(VTypeHelper.getDouble((VType) newValue));
            return false;
        });

        setPropertyChangeHandler(MyWidgetModel.PROP_MAX, (oldValue, newValue, figure) -> {
            ((MyWidgetFigure) figure).setMax((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(MyWidgetModel.PROP_MIN, (oldValue, newValue, figure) -> {
            ((MyWidgetFigure) figure).setMin((Double) newValue);
            return false;
        });
    }

    @Override
    public Object getValue() {
        return ((MyWidgetFigure) getFigure()).getValue();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Double) {
            ((MyWidgetFigure) getFigure()).setValue((Double) value);
        }
    }
}
