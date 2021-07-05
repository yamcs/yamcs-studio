package org.csstudio.opibuilder.widgets.editparts;

import java.text.DecimalFormat;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.widgets.model.LabelModel;
import org.csstudio.opibuilder.widgets.model.SpinnerModel;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.Display;
import org.yamcs.studio.data.vtype.VType;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.csstudio.swt.widgets.figures.SpinnerFigure;
import org.csstudio.swt.widgets.figures.SpinnerFigure.NumericFormatType;
import org.csstudio.swt.widgets.figures.TextFigure;
import org.csstudio.swt.widgets.figures.TextFigure.H_ALIGN;
import org.csstudio.swt.widgets.figures.TextFigure.V_ALIGN;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.tools.SelectEditPartTracker;

public class SpinnerEditpart extends AbstractPVWidgetEditPart {

    private IPVListener pvLoadLimitsListener;
    private Display meta = null;
    private IPVListener pvLoadPrecisionListener;

    @Override
    protected IFigure doCreateFigure() {
        SpinnerFigure spinner = new SpinnerFigure();
        TextFigure labelFigure = spinner.getLabelFigure();
        labelFigure.setFont(getWidgetModel().getFont().getSWTFont());
        labelFigure.setFontPixels(getWidgetModel().getFont().isSizeInPixels());
        labelFigure.setOpaque(!getWidgetModel().isTransparent());
        labelFigure.setHorizontalAlignment(getWidgetModel().getHorizontalAlignment());
        labelFigure.setVerticalAlignment(getWidgetModel().getVerticalAlignment());
        spinner.setMax(getWidgetModel().getMaximum());
        spinner.setMin(getWidgetModel().getMinimum());
        spinner.setStepIncrement(getWidgetModel().getStepIncrement());
        spinner.setPageIncrement(getWidgetModel().getPageIncrement());
        spinner.setFormatType(getWidgetModel().getFormat());
        spinner.setPrecision((Integer) getPropertyValue(SpinnerModel.PROP_PRECISION));
        spinner.setArrowButtonsOnLeft(getWidgetModel().isButtonsOnLeft());
        spinner.setArrowButtonsHorizontal(getWidgetModel().isHorizontalButtonsLayout());
        spinner.showText(getWidgetModel().showText());

        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            spinner.addManualValueChangeListener(newValue -> {
                setPVValue(SpinnerModel.PROP_PVNAME, newValue);
                getWidgetModel().setText(((SpinnerFigure) getFigure()).getLabelFigure().getText(), false);
            });
        }

        return spinner;
    }

    @Override
    public SpinnerModel getWidgetModel() {
        return (SpinnerModel) getModel();
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new SpinnerDirectEditPolicy());
    }

    @Override
    public void activate() {
        markAsControlPV(AbstractPVWidgetModel.PROP_PVNAME, AbstractPVWidgetModel.PROP_PVVALUE);
        super.activate();
    }

    @Override
    protected void doActivate() {
        super.doActivate();
        registerLoadLimitsListener();
    }

    private void registerLoadLimitsListener() {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            final SpinnerModel model = getWidgetModel();
            if (model.isLimitsFromPV() || model.isPrecisionFromPV()) {
                IPV pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
                if (pv != null) {
                    if (pvLoadLimitsListener == null) {
                        pvLoadLimitsListener = new IPVListener.Stub() {
                            @Override
                            public void valueChanged(IPV pv) {
                                VType value = pv.getValue();
                                Display displayInfo = VTypeHelper.getDisplayInfo(value);
                                if (value != null && displayInfo != null) {
                                    Display new_meta = displayInfo;
                                    if (meta == null || !meta.equals(new_meta)) {
                                        meta = new_meta;
                                        if (model.isLimitsFromPV()) {
                                            model.setPropertyValue(SpinnerModel.PROP_MAX,
                                                    meta.getUpperCtrlLimit());
                                            model.setPropertyValue(SpinnerModel.PROP_MIN,
                                                    meta.getLowerCtrlLimit());
                                        }
                                        if (model.isPrecisionFromPV()) {
                                            model.setPropertyValue(SpinnerModel.PROP_PRECISION,
                                                    meta.getFormat().getMaximumFractionDigits());
                                        }
                                    }
                                }
                            }
                        };
                    }
                    pv.addListener(pvLoadLimitsListener);
                }
            }
        }
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        // text
        IWidgetPropertyChangeHandler handler = (oldValue, newValue, figure) -> {
            String text = (String) newValue;
            try {
                text = text.replace("e", "E");
                double value = new DecimalFormat().parse(text).doubleValue();
                // coerce value in range
                value = Math.max(((SpinnerFigure) figure).getMin(),
                        Math.min(((SpinnerFigure) figure).getMax(), value));
                ((SpinnerFigure) figure).setValue(value);
                if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                    setPVValue(AbstractPVWidgetModel.PROP_PVNAME, value);
                }
                getWidgetModel().setText(
                        ((SpinnerFigure) figure).getLabelFigure().getText(), false);
                return false;
            } catch (Exception e) {
                return false;
            }
        };
        setPropertyChangeHandler(SpinnerModel.PROP_TEXT, handler);

        IWidgetPropertyChangeHandler pvNameHandler = (oldValue, newValue, figure) -> {
            registerLoadLimitsListener();
            return false;
        };
        setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVNAME, pvNameHandler);

        // pv value
        handler = (oldValue, newValue, figure) -> {
            if (newValue == null) {
                return false;
            }
            double value = VTypeHelper.getDouble((VType) newValue);
            ((SpinnerFigure) figure).setDisplayValue(value);
            getWidgetModel().setText(((SpinnerFigure) figure).getLabelFigure().getText(), false);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_PVVALUE, handler);

        // min
        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setMin((Double) newValue);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_MIN, handler);

        // max
        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setMax((Double) newValue);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_MAX, handler);

        // step increment
        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setStepIncrement((Double) newValue);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_STEP_INCREMENT, handler);

        // page increment
        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setPageIncrement((Double) newValue);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_PAGE_INCREMENT, handler);

        // font
        IWidgetPropertyChangeHandler fontHandler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).getLabelFigure().setFont(CustomMediaFactory.getInstance().getFont(
                    ((OPIFont) newValue).getFontData()));
            return true;
        };
        setPropertyChangeHandler(LabelModel.PROP_FONT, fontHandler);

        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).getLabelFigure().setHorizontalAlignment(H_ALIGN.values()[(Integer) newValue]);
            return true;
        };
        setPropertyChangeHandler(LabelModel.PROP_ALIGN_H, handler);

        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).getLabelFigure().setVerticalAlignment(V_ALIGN.values()[(Integer) newValue]);
            return true;
        };
        setPropertyChangeHandler(LabelModel.PROP_ALIGN_V, handler);

        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).getLabelFigure().setOpaque(!(Boolean) newValue);
            return true;
        };
        setPropertyChangeHandler(LabelModel.PROP_TRANSPARENT, handler);

        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setFormatType(NumericFormatType.values()[(Integer) newValue]);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_FORMAT, handler);

        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setPrecision((Integer) newValue);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_PRECISION, handler);

        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setArrowButtonsOnLeft((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_BUTTONS_ON_LEFT, handler);

        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setArrowButtonsHorizontal((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_HORIZONTAL_BUTTONS_LAYOUT, handler);

        handler = (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).showText((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(SpinnerModel.PROP_SHOW_TEXT, handler);
    }

    @Override
    public DragTracker getDragTracker(Request request) {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            return new SelectEditPartTracker(this) {
                @Override
                protected boolean handleButtonUp(int button) {
                    if (button == 1) {
                        // make widget in edit mode by single click
                        performOpen();
                    }
                    return super.handleButtonUp(button);
                }
            };
        } else {
            return super.getDragTracker(request);
        }
    }

    @Override
    public void performRequest(Request request) {
        if (getFigure().isEnabled() && getWidgetModel().showText()
                && ((request.getType() == RequestConstants.REQ_DIRECT_EDIT
                        && getExecutionMode() != ExecutionMode.RUN_MODE)
                        || request.getType() == RequestConstants.REQ_OPEN)) {
            performDirectEdit();
        }
    }

    protected void performDirectEdit() {
        new SpinnerTextEditManager(this,
                new LabelCellEditorLocator(
                        ((SpinnerFigure) getFigure()).getLabelFigure()),
                false, ((SpinnerFigure) figure).getStepIncrement(), ((SpinnerFigure) figure).getPageIncrement()).show();
    }

    @Override
    protected void doDeActivate() {
        super.doDeActivate();
        if (getWidgetModel().isLimitsFromPV()) {
            IPV pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
            if (pv != null && pvLoadLimitsListener != null) {
                pv.removeListener(pvLoadLimitsListener);
            }
            if (pv != null && pvLoadPrecisionListener != null) {
                pv.removeListener(pvLoadPrecisionListener);
            }
        }
    }

    @Override
    public Double getValue() {
        return ((SpinnerFigure) getFigure()).getValue();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((SpinnerFigure) getFigure()).setValue(((Number) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class key) {
        if (key == ITextFigure.class) {
            return ((SpinnerFigure) getFigure()).getLabelFigure();
        }

        return super.getAdapter(key);
    }
}
