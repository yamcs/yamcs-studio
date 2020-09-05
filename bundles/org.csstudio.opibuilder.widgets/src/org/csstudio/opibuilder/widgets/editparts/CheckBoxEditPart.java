package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.CheckBoxModel;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VType;
import org.csstudio.swt.widgets.figures.AbstractBoolFigure.TotalBits;
import org.csstudio.swt.widgets.figures.CheckBoxFigure;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.swt.widgets.Display;

public class CheckBoxEditPart extends AbstractPVWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        CheckBoxFigure figure = new CheckBoxFigure(getExecutionMode().equals(
                ExecutionMode.RUN_MODE));
        figure.setBit(getWidgetModel().getBit());
        figure.setText(getWidgetModel().getLabel());
        figure.setSelectedColor(getWidgetModel().getSelectedColor().getSWTColor());
        figure.addManualValueChangeListener(newValue -> {
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                setPVValue(AbstractPVWidgetModel.PROP_PVNAME, newValue);
            }
        });
        markAsControlPV(AbstractPVWidgetModel.PROP_PVNAME, AbstractPVWidgetModel.PROP_PVVALUE);

        return figure;
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        if (getExecutionMode() == ExecutionMode.EDIT_MODE) {
            installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new TextDirectEditPolicy());
        }
    }

    protected void performDirectEdit() {
        new TextEditManager(this,
                new LabelCellEditorLocator(getFigure()), false).show();
    }

    @Override
    public void performRequest(Request request) {
        if (getExecutionMode() == ExecutionMode.EDIT_MODE && (request.getType() == RequestConstants.REQ_DIRECT_EDIT ||
                request.getType() == RequestConstants.REQ_OPEN)) {
            performDirectEdit();
        }
    }

    @Override
    public CheckBoxModel getWidgetModel() {
        return (CheckBoxModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {

        // value
        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            if (newValue == null) {
                return false;
            }
            CheckBoxFigure figure = (CheckBoxFigure) refreshableFigure;

            switch (VTypeHelper.getBasicDataType((VType) newValue)) {
            case SHORT:
                figure.setTotalBits(TotalBits.BITS_16);
                break;
            case INT:
            case ENUM:
                figure.setTotalBits(TotalBits.BITS_32);
                break;
            default:
                break;
            }

            figure.setValue(VTypeHelper.getDouble((VType) newValue));
            return true;
        };
        setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVVALUE, handler);

        // bit
        handler = (oldValue, newValue, refreshableFigure) -> {
            CheckBoxFigure figure = (CheckBoxFigure) refreshableFigure;
            figure.setBit((Integer) newValue);
            return true;
        };
        setPropertyChangeHandler(CheckBoxModel.PROP_BIT, handler);

        // label
        handler = (oldValue, newValue, refreshableFigure) -> {
            CheckBoxFigure figure = (CheckBoxFigure) refreshableFigure;
            figure.setText((String) newValue);
            Display.getCurrent().timerExec(10, () -> {
                if (getWidgetModel().isAutoSize()) {
                    performAutoSize(refreshableFigure);
                }
            });
            return true;
        };
        setPropertyChangeHandler(CheckBoxModel.PROP_LABEL, handler);

        handler = (oldValue, newValue, figure) -> {
            if ((Boolean) newValue) {
                performAutoSize(figure);
                figure.revalidate();
            }
            return true;
        };
        setPropertyChangeHandler(CheckBoxModel.PROP_AUTOSIZE, handler);

        handler = (oldValue, newValue, figure) -> {
            ((CheckBoxFigure) figure).setSelectedColor(
                    getWidgetModel().getSelectedColor().getSWTColor());
            return true;
        };
        setPropertyChangeHandler(CheckBoxModel.PROP_SELECTED_COLOR, handler);

        handler = (oldValue, newValue, figure) -> {
            Display.getCurrent().timerExec(10, () -> {
                if (getWidgetModel().isAutoSize()) {
                    performAutoSize(figure);
                    figure.revalidate();
                }
            });

            return true;
        };
        setPropertyChangeHandler(CheckBoxModel.PROP_FONT, handler);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_STYLE, handler);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_WIDTH, handler);
    }

    private void performAutoSize(IFigure figure) {
        getWidgetModel().setSize(((CheckBoxFigure) figure).getAutoSizeDimension());
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((CheckBoxFigure) getFigure()).setValue(((Number) value).longValue());
        } else if (value instanceof Boolean) {
            ((CheckBoxFigure) getFigure()).setBoolValue((Boolean) value);
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Boolean getValue() {
        return ((CheckBoxFigure) getFigure()).getBoolValue();
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
        if (key == ITextFigure.class) {
            return getFigure();
        }

        return super.getAdapter(key);
    }
}
