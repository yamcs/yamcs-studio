package org.csstudio.opibuilder.widgets.editparts;

import java.util.List;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.BoolButtonModel;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VType;
import org.csstudio.swt.widgets.figures.BoolButtonFigure;
import org.eclipse.draw2d.IFigure;

public class BoolButtonEditPart extends AbstractBoolControlEditPart {

    private IPVListener loadLabelsFromPVListener;

    @Override
    protected IFigure doCreateFigure() {
        final BoolButtonModel model = getWidgetModel();

        BoolButtonFigure btn = new BoolButtonFigure();

        initializeCommonFigureProperties(btn, model);
        btn.setEffect3D(model.isEffect3D());
        btn.setSquareButton((model.isSquareButton()));
        btn.setShowLED(model.isShowLED());
        return btn;
    }

    @Override
    public BoolButtonModel getWidgetModel() {
        return (BoolButtonModel) getModel();
    }

    @Override
    protected void doActivate() {
        super.doActivate();
        // Must be called after the PV objects are created and
        // so cannot be called in the registerPropertyChangeHandlers()
        // method.
        registerLoadLabelsListener();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        // effect 3D
        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            BoolButtonFigure btn = (BoolButtonFigure) refreshableFigure;
            btn.setEffect3D((Boolean) newValue);
            return true;
        };
        setPropertyChangeHandler(BoolButtonModel.PROP_EFFECT3D, handler);

        // Square LED
        handler = (oldValue, newValue, refreshableFigure) -> {
            BoolButtonFigure btn = (BoolButtonFigure) refreshableFigure;
            btn.setSquareButton((Boolean) newValue);
            return true;
        };
        setPropertyChangeHandler(BoolButtonModel.PROP_SQUARE_BUTTON, handler);

        // Show LED
        handler = (oldValue, newValue, refreshableFigure) -> {
            BoolButtonFigure btn = (BoolButtonFigure) refreshableFigure;
            btn.setShowLED((Boolean) newValue);
            return true;
        };
        setPropertyChangeHandler(BoolButtonModel.PROP_SHOW_LED, handler);
    }

    /**
     * Load the button labels from the PV object. This must be called after the PV objects are created.
     */
    private void registerLoadLabelsListener() {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            if (getWidgetModel().isLabelsFromPV()) {
                IPV pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
                if (pv != null) {
                    if (loadLabelsFromPVListener == null) {
                        loadLabelsFromPVListener = new IPVListener.Stub() {
                            @Override
                            public void valueChanged(IPV pv) {
                                VType value = pv.getValue();
                                if (value != null && value instanceof VEnum) {
                                    List<String> enumLabels = ((VEnum) value).getLabels();
                                    // This is a bool button so we require exactly two labels.
                                    if (enumLabels.size() > 0) {
                                        getWidgetModel().setPropertyValue(BoolButtonModel.PROP_OFF_LABEL,
                                                enumLabels.get(0));
                                    }
                                    if (enumLabels.size() > 1) {
                                        getWidgetModel().setPropertyValue(BoolButtonModel.PROP_ON_LABEL,
                                                enumLabels.get(1));
                                    }
                                }
                            }
                        };
                    }
                    pv.addListener(loadLabelsFromPVListener);
                }
            }
        }
    }
}
