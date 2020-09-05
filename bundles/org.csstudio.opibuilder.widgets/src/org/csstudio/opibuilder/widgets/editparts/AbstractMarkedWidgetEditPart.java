package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel;
import org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.Display;
import org.yamcs.studio.data.vtype.VType;
import org.csstudio.swt.widgets.figures.AbstractMarkedWidgetFigure;

/**
 * Base editPart controller for a widget based on {@link AbstractMarkedWidgetModel}.
 */
public abstract class AbstractMarkedWidgetEditPart extends AbstractScaledWidgetEditPart {

    private Display meta = null;
    private IPVListener pvLoadLimitsListener;

    /**
     * Sets those properties on the figure that are defined in the {@link AbstractMarkedWidgetFigure} base class. This
     * method is provided for the convenience of subclasses, which can call this method in their implementation of
     * {@link AbstractBaseEditPart#doCreateFigure()}.
     *
     * @param figure
     *            the figure.
     * @param model
     *            the model.
     */
    protected void initializeCommonFigureProperties(
            final AbstractMarkedWidgetFigure figure, final AbstractMarkedWidgetModel model) {

        super.initializeCommonFigureProperties(figure, model);
        figure.setShowMarkers(model.isShowMarkers());

        figure.setLoloLevel(model.getLoloLevel());
        figure.setLoLevel(model.getLoLevel());
        figure.setHiLevel(model.getHiLevel());
        figure.setHihiLevel(model.getHihiLevel());

        figure.setShowLolo(model.isShowLolo());
        figure.setShowLo(model.isShowLo());
        figure.setShowHi(model.isShowHi());
        figure.setShowHihi(model.isShowHihi());

        figure.setLoloColor(model.getLoloColor());
        figure.setLoColor(model.getLoColor());
        figure.setHiColor(model.getHiColor());
        figure.setHihiColor(model.getHihiColor());

    }

    @Override
    protected void doActivate() {
        super.doActivate();
        registerLoadLimitsListener();
    }

    /**
     *
     */
    private void registerLoadLimitsListener() {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            final AbstractMarkedWidgetModel model = (AbstractMarkedWidgetModel) getModel();
            if (model.isLimitsFromPV()) {
                IPV pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
                if (pv != null) {
                    if (pvLoadLimitsListener == null) {
                        pvLoadLimitsListener = new IPVListener.Stub() {
                            @Override
                            public void valueChanged(IPV pv) {
                                VType value = pv.getValue();
                                if (value != null && VTypeHelper.getDisplayInfo(value) != null) {
                                    Display new_meta = VTypeHelper.getDisplayInfo(value);
                                    if (meta == null || !meta.equals(new_meta)) {
                                        meta = new_meta;

                                        Double upperLimit;
                                        Double lowerLimit;
                                        if (model.isControlWidget()) {
                                            // DRVH / DRVL
                                            upperLimit = meta.getUpperCtrlLimit();
                                            lowerLimit = meta.getLowerCtrlLimit();
                                        } else {
                                            // HOPR / LOPR
                                            upperLimit = meta.getUpperDisplayLimit();
                                            lowerLimit = meta.getLowerDisplayLimit();
                                        }
                                        if (!Double.isNaN(upperLimit)) {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_MAX, upperLimit);
                                        }
                                        if (!Double.isNaN(lowerLimit)) {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_MIN, lowerLimit);
                                        }

                                        if (Double.isNaN(meta.getUpperWarningLimit())) {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_SHOW_HI, false);
                                        } else {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_SHOW_HI, true);
                                            model.setPropertyValue(
                                                    AbstractMarkedWidgetModel.PROP_HI_LEVEL,
                                                    meta.getUpperWarningLimit());
                                        }
                                        if (Double.isNaN(meta.getUpperAlarmLimit())) {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_SHOW_HIHI, false);
                                        } else {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_SHOW_HIHI, true);
                                            model.setPropertyValue(
                                                    AbstractMarkedWidgetModel.PROP_HIHI_LEVEL,
                                                    meta.getUpperAlarmLimit());
                                        }
                                        if (Double.isNaN(meta.getLowerWarningLimit())) {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_SHOW_LO, false);
                                        } else {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_SHOW_LO, true);
                                            model.setPropertyValue(
                                                    AbstractMarkedWidgetModel.PROP_LO_LEVEL,
                                                    meta.getLowerWarningLimit());
                                        }
                                        if (Double.isNaN(meta.getLowerAlarmLimit())) {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_SHOW_LOLO, false);
                                        } else {
                                            model.setPropertyValue(AbstractMarkedWidgetModel.PROP_SHOW_LOLO, true);
                                            model.setPropertyValue(
                                                    AbstractMarkedWidgetModel.PROP_LOLO_LEVEL,
                                                    meta.getLowerAlarmLimit());
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
    public AbstractMarkedWidgetModel getWidgetModel() {
        return (AbstractMarkedWidgetModel) getModel();
    }

    @Override
    protected void doDeActivate() {
        super.doDeActivate();
        if (getWidgetModel().isLimitsFromPV()) {
            IPV pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
            if (pv != null && pvLoadLimitsListener != null) {
                pv.removeListener(pvLoadLimitsListener);
            }
        }

    }

    /**
     * Registers property change handlers for the properties defined in {@link AbstractScaledWidgetModel}. This method
     * is provided for the convenience of subclasses, which can call this method in their implementation of
     * {@link #registerPropertyChangeHandlers()}.
     */
    @Override
    protected void registerCommonPropertyChangeHandlers() {
        super.registerCommonPropertyChangeHandlers();

        IWidgetPropertyChangeHandler pvNameHandler = (oldValue, newValue, figure) -> {
            registerLoadLimitsListener();
            return false;
        };
        setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVNAME, pvNameHandler);

        // showMarkers
        IWidgetPropertyChangeHandler showMarkersHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowMarkers((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_SHOW_MARKERS, showMarkersHandler);

        // LoLo Level
        IWidgetPropertyChangeHandler loloHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setLoloLevel((Double) newValue);
            return true;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_LOLO_LEVEL, loloHandler);

        // Lo Level
        IWidgetPropertyChangeHandler loHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setLoLevel((Double) newValue);
            return true;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_LO_LEVEL, loHandler);

        // Hi Level
        IWidgetPropertyChangeHandler hiHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setHiLevel((Double) newValue);
            return true;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_HI_LEVEL, hiHandler);

        // HiHi Level
        IWidgetPropertyChangeHandler hihiHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setHihiLevel((Double) newValue);
            return true;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_HIHI_LEVEL, hihiHandler);

        // show lolo
        IWidgetPropertyChangeHandler showLoloHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowLolo((Boolean) newValue);
            return true;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_SHOW_LOLO, showLoloHandler);

        // show lo
        IWidgetPropertyChangeHandler showLoHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowLo((Boolean) newValue);
            return true;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_SHOW_LO, showLoHandler);

        // show Hi
        IWidgetPropertyChangeHandler showHiHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowHi((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_SHOW_HI, showHiHandler);

        // show Hihi
        IWidgetPropertyChangeHandler showHihiHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowHihi((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_SHOW_HIHI, showHihiHandler);

        // Lolo color
        IWidgetPropertyChangeHandler LoloColorHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setLoloColor(((OPIColor) newValue).getSWTColor());
            return false;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_LOLO_COLOR, LoloColorHandler);

        // Lo color
        IWidgetPropertyChangeHandler LoColorHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setLoColor(((OPIColor) newValue).getSWTColor());
            return false;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_LO_COLOR, LoColorHandler);

        // Hi color
        IWidgetPropertyChangeHandler HiColorHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setHiColor(((OPIColor) newValue).getSWTColor());
            return false;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_HI_COLOR, HiColorHandler);

        // Hihi color
        IWidgetPropertyChangeHandler HihiColorHandler = (oldValue, newValue, refreshableFigure) -> {
            AbstractMarkedWidgetFigure figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setHihiColor(((OPIColor) newValue).getSWTColor());
            return false;
        };
        setPropertyChangeHandler(AbstractMarkedWidgetModel.PROP_HIHI_COLOR, HihiColorHandler);
    }
}
