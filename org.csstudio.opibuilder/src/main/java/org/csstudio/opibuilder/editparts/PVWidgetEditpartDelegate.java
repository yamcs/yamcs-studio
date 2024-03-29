/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.editparts;

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_COLOR_BACKGROUND;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_COLOR_FOREGROUND;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_ALARM_PULSING;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_BACKCOLOR_ALARMSENSITIVE;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_BORDER_ALARMSENSITIVE;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_FORECOLOR_ALARMSENSITIVE;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.csstudio.java.thread.ExecutionService;
import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.IPVWidgetModel;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.AlarmRepresentationScheme;
import org.csstudio.opibuilder.util.BOYPVFactory;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPITimer;
import org.csstudio.opibuilder.visualparts.BorderFactory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.EditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.AlarmSeverity;
import org.yamcs.studio.data.vtype.VType;

public class PVWidgetEditpartDelegate implements IPVWidgetEditpart {
    // private interface AlarmSeverity extends ISeverity{
    // public void copy(ISeverity severity);
    // }
    private final class WidgetPVListener implements IPVListener {
        private String pvPropID;
        private boolean isControlPV;

        public WidgetPVListener(String pvPropID) {
            this.pvPropID = pvPropID;
            isControlPV = pvPropID.equals(controlPVPropId);
        }

        @Override
        public void connectionChanged(IPV pv) {
            if (!pv.isConnected()) {
                lastWriteAccess = null;
            }
        }

        @Override
        public void valueChanged(IPV pv) {
            var widgetModel = editpart.getWidgetModel();

            // write access
            // if(isControlPV)
            // updateWritable(widgetModel, pv);

            if (pv.getValue() != null) {
                if (ignoreOldPVValue) {
                    widgetModel.getPVMap().get(widgetModel.getProperty(pvPropID))
                            .setPropertyValue_IgnoreOldValue(pv.getValue());
                } else {
                    widgetModel.getPVMap().get(widgetModel.getProperty(pvPropID)).setPropertyValue(pv.getValue());
                }
            }
        }

        @Override
        public void writePermissionChanged(IPV pv) {
            if (isControlPV) {
                updateWritable(editpart.getWidgetModel(), pvMap.get(pvPropID));
            }
        }
    }

    // invisible border for no_alarm state, this can prevent the widget from resizing
    // when alarm turn back to no_alarm state/
    private static final AbstractBorder BORDER_NO_ALARM = new AbstractBorder() {

        @Override
        public Insets getInsets(IFigure figure) {
            return new Insets(2);
        }

        @Override
        public void paint(IFigure figure, Graphics graphics, Insets insets) {
        }
    };

    private int updateSuppressTime = 1000;
    private String controlPVPropId = null;

    private String controlPVValuePropId = null;
    /**
     * In most cases, old pv value in the valueChange() method of {@link IWidgetPropertyChangeHandler} is not useful.
     * Ignore the old pv value will help to reduce memory usage.
     */
    private boolean ignoreOldPVValue = true;
    private boolean isBackColorAlarmSensitive;

    private boolean isBorderAlarmSensitive;
    private boolean isForeColorAlarmSensitive;
    private AlarmSeverity alarmSeverity = AlarmSeverity.NONE;

    private Map<String, IPVListener> pvListenerMap = new HashMap<>();

    private Map<String, IPV> pvMap = new HashMap<>();
    private PropertyChangeListener[] pvValueListeners;
    private AbstractBaseEditPart editpart;
    private volatile AtomicBoolean lastWriteAccess;
    private Cursor savedCursor;

    private Color saveForeColor, saveBackColor;
    // the task which will be executed when the updateSuppressTimer due.
    protected Runnable timerTask;

    // The update from PV will be suppressed for a brief time when writing was performed
    protected OPITimer updateSuppressTimer;
    private IPVWidgetModel widgetModel;

    private ListenerList<ISetPVValueListener> setPVValueListeners;
    private ListenerList<AlarmSeverityListener> alarmSeverityListeners;
    private boolean isAlarmPulsing = false;
    private ScheduledFuture<?> scheduledFuture;

    private boolean pvsHaveBeenStarted = false;

    /**
     * @param editpart
     *            the editpart to be delegated. It must implemented {@link IPVWidgetEditpart}
     */
    public PVWidgetEditpartDelegate(AbstractBaseEditPart editpart) {
        this.editpart = editpart;
    }

    public IPVWidgetModel getWidgetModel() {
        if (widgetModel == null) {
            widgetModel = (IPVWidgetModel) editpart.getWidgetModel();
        }
        return widgetModel;
    }

    public void doActivate() {
        saveFigureOKStatus(editpart.getFigure());
        if (editpart.getExecutionMode() == ExecutionMode.RUN_MODE) {
            pvMap.clear();
            var pvPropertyMap = editpart.getWidgetModel().getPVMap();

            for (var sp : pvPropertyMap.keySet()) {
                if (sp.getPropertyValue() == null || sp.getPropertyValue().trim().length() <= 0) {
                    continue;
                }

                try {
                    var pv = BOYPVFactory.createPV(sp.getPropertyValue());
                    pvMap.put(sp.getPropertyID(), pv);
                    editpart.addToConnectionHandler(sp.getPropertyValue(), pv);
                    var pvListener = new WidgetPVListener(sp.getPropertyID());
                    pv.addListener(pvListener);
                    pvListenerMap.put(sp.getPropertyID(), pvListener);
                } catch (Exception e) {
                    OPIBuilderPlugin.getLogger().log(Level.WARNING,
                            "Unable to connect to PV:" + sp.getPropertyValue(), e);
                }
            }
        }
    }

    /**
     * Start all PVs. This should be called as the last step in editpart.activate().
     */
    public void startPVs() {
        pvsHaveBeenStarted = true;
        // the pv should be started at the last minute
        for (var pvPropId : pvMap.keySet()) {
            var pv = pvMap.get(pvPropId);
            try {
                pv.start();
            } catch (Exception e) {
                OPIBuilderPlugin.getLogger().log(Level.WARNING, "Unable to connect to PV:" + pv.getName(), e);
            }
        }
    }

    public void doDeActivate() {
        if (pvsHaveBeenStarted) {
            for (var pv : pvMap.values()) {
                pv.stop();
            }
            pvsHaveBeenStarted = false;
        }
        for (var pvPropID : pvListenerMap.keySet()) {
            pvMap.get(pvPropID).removeListener(pvListenerMap.get(pvPropID));
        }

        pvMap.clear();
        pvListenerMap.clear();
        stopPulsing();
    }

    @Override
    public IPV getControlPV() {
        if (controlPVPropId != null) {
            return pvMap.get(controlPVPropId);
        }
        return null;
    }

    /**
     * Get the PV corresponding to the <code>PV Name</code> property. It is same as calling
     * <code>getPV("pv_name")</code>.
     *
     * @return the PV corresponding to the <code>PV Name</code> property. null if PV Name is not configured for this
     *         widget.
     */
    @Override
    public IPV getPV() {
        return pvMap.get(IPVWidgetModel.PROP_PVNAME);
    }

    /**
     * Get the pv by PV property id.
     *
     * @param pvPropId
     *            the PV property id.
     * @return the corresponding pv for the pvPropId. null if the pv doesn't exist.
     */
    @Override
    public IPV getPV(String pvPropId) {
        return pvMap.get(pvPropId);
    }

    /**
     * Get value from one of the attached PVs.
     *
     * @param pvPropId
     *            the property id of the PV. It is "pv_name" for the main PV.
     * @return the IValue of the PV.
     */
    @Override
    public VType getPVValue(String pvPropId) {
        var pv = pvMap.get(pvPropId);
        if (pv != null) {
            return pv.getValue();
        }
        return null;
    }

    /**
     * @return the time needed to suppress reading back from PV after writing. No need to suppress if returned value <=0
     */
    public int getUpdateSuppressTime() {
        return updateSuppressTime;
    }

    /**
     * Set the time needed to suppress reading back from PV after writing. No need to suppress if returned value <=0
     *
     * @param updateSuppressTime
     */
    public void setUpdateSuppressTime(int updateSuppressTime) {
        this.updateSuppressTime = updateSuppressTime;
    }

    public void initFigure(IFigure figure) {
        // initialize frequent used variables
        isBorderAlarmSensitive = getWidgetModel().isBorderAlarmSensitve();
        isBackColorAlarmSensitive = getWidgetModel().isBackColorAlarmSensitve();
        isForeColorAlarmSensitive = getWidgetModel().isForeColorAlarmSensitve();
        isAlarmPulsing = getWidgetModel().isAlarmPulsing();

        if (isBorderAlarmSensitive && editpart.getWidgetModel().getBorderStyle() == BorderStyle.NONE) {
            editpart.setFigureBorder(BORDER_NO_ALARM);
        }
    }

    /**
     * Initialize the updateSuppressTimer
     */
    private synchronized void initUpdateSuppressTimer() {
        if (updateSuppressTimer == null) {
            updateSuppressTimer = new OPITimer();
        }
        if (timerTask == null) {
            timerTask = () -> {
                var pvValueProperty = editpart.getWidgetModel().getProperty(controlPVValuePropId);
                // recover update
                if (pvValueListeners != null) {
                    for (var listener : pvValueListeners) {
                        pvValueProperty.addPropertyChangeListener(listener);
                    }
                }
                // forcefully set PV_Value property again
                pvValueProperty.setPropertyValue(pvValueProperty.getPropertyValue(), true);
            };
        }
    }

    /**
     * For PV Control widgets, mark this PV as control PV.
     *
     * @param pvPropId
     *            the propId of the PV.
     */
    public void markAsControlPV(String pvPropId, String pvValuePropId) {
        controlPVPropId = pvPropId;
        controlPVValuePropId = pvValuePropId;
        initUpdateSuppressTimer();
    }

    @Override
    public boolean isPVControlWidget() {
        return controlPVPropId != null;
    }

    public void registerBasePropertyChangeHandlers() {
        editpart.setPropertyChangeHandler(PROP_BORDER_ALARMSENSITIVE, (oldValue, newValue, figure) -> {
            editpart.setFigureBorder(editpart.calculateBorder());
            return true;
        });

        editpart.setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, figure) -> {
            // No valid value is given. Do nothing.
            if (newValue == null || !(newValue instanceof VType)) {
                return false;
            }

            var newSeverity = VTypeHelper.getAlarmSeverity((VType) newValue);
            if (newSeverity == null) {
                return false;
            }

            if (newSeverity != alarmSeverity) {
                alarmSeverity = newSeverity;
                fireAlarmSeverityChanged(newSeverity, figure);
            }
            return true;
        });

        // Border Alarm Sensitive
        addAlarmSeverityListener((severity, figure) -> {
            if (!isBorderAlarmSensitive) {
                return false;
            }

            editpart.setFigureBorder(editpart.calculateBorder());
            return true;
        });

        // BackColor Alarm Sensitive
        addAlarmSeverityListener((severity, figure) -> {
            if (!isBackColorAlarmSensitive) {
                return false;
            }
            figure.setBackgroundColor(calculateBackColor());
            return true;
        });

        // ForeColor Alarm Sensitive
        addAlarmSeverityListener((severity, figure) -> {
            if (!isForeColorAlarmSensitive) {
                return false;
            }
            figure.setForegroundColor(calculateForeColor());
            return true;
        });

        // Pulsing Alarm Sensitive
        addAlarmSeverityListener((severity, figure) -> {
            if (!isAlarmPulsing) {
                return false;
            }
            if (severity == AlarmSeverity.MAJOR || severity == AlarmSeverity.MINOR) {
                startPulsing();
            } else {
                stopPulsing();
            }
            return true;
        });

        class PVNamePropertyChangeHandler implements IWidgetPropertyChangeHandler {
            private String pvNamePropID;

            public PVNamePropertyChangeHandler(String pvNamePropID) {
                this.pvNamePropID = pvNamePropID;
            }

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                var oldPV = pvMap.get(pvNamePropID);
                editpart.removeFromConnectionHandler((String) oldValue);
                if (oldPV != null) {
                    oldPV.stop();
                    oldPV.removeListener(pvListenerMap.get(pvNamePropID));
                }
                pvMap.remove(pvNamePropID);
                var newPVName = ((String) newValue).trim();
                if (newPVName.length() <= 0) {
                    return false;
                }
                try {
                    lastWriteAccess = null;
                    var newPV = BOYPVFactory.createPV(newPVName);
                    var pvListener = new WidgetPVListener(pvNamePropID);
                    newPV.addListener(pvListener);
                    pvMap.put(pvNamePropID, newPV);
                    editpart.addToConnectionHandler(newPVName, newPV);
                    pvListenerMap.put(pvNamePropID, pvListener);

                    newPV.start();
                } catch (Exception e) {
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, "Unable to connect to PV:" + newPVName, e);
                }

                return false;
            }
        }
        // PV name
        for (var pvNameProperty : editpart.getWidgetModel().getPVMap().keySet()) {
            if (editpart.getExecutionMode() == ExecutionMode.RUN_MODE) {
                editpart.setPropertyChangeHandler(pvNameProperty.getPropertyID(),
                        new PVNamePropertyChangeHandler(pvNameProperty.getPropertyID()));
            }
        }

        if (editpart.getExecutionMode() == ExecutionMode.EDIT_MODE) {
            editpart.getWidgetModel().getProperty(PROP_PVNAME).addPropertyChangeListener(evt -> {
                // reselect the widget to update feedback.
                var selected = editpart.getSelected();
                if (selected != EditPart.SELECTED_NONE) {
                    editpart.setSelected(EditPart.SELECTED_NONE);
                    editpart.setSelected(selected);
                }
            });
        }

        editpart.setPropertyChangeHandler(PROP_COLOR_BACKGROUND, (oldValue, newValue, figure) -> {
            saveBackColor = ((OPIColor) newValue).getSWTColor();
            return false;
        });

        editpart.setPropertyChangeHandler(PROP_COLOR_FOREGROUND, (oldValue, newValue, figure) -> {
            saveForeColor = ((OPIColor) newValue).getSWTColor();
            return false;
        });

        editpart.setPropertyChangeHandler(PROP_BACKCOLOR_ALARMSENSITIVE,
                (oldValue, newValue, figure) -> {
                    isBackColorAlarmSensitive = (Boolean) newValue;
                    figure.setBackgroundColor(calculateBackColor());
                    return true;
                });

        editpart.setPropertyChangeHandler(PROP_FORECOLOR_ALARMSENSITIVE,
                (oldValue, newValue, figure) -> {
                    isForeColorAlarmSensitive = (Boolean) newValue;
                    figure.setForegroundColor(calculateForeColor());
                    return true;
                });

        editpart.setPropertyChangeHandler(PROP_ALARM_PULSING, (oldValue, newValue, figure) -> {
            isAlarmPulsing = (Boolean) newValue;
            stopPulsing();
            fireAlarmSeverityChanged(alarmSeverity, figure);
            return true;
        });
    }

    public synchronized void stopPulsing() {
        if (scheduledFuture != null) {
            // stop the pulsing runnable
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    public synchronized void startPulsing() {
        stopPulsing();
        Runnable pulsingTask = () -> UIBundlingThread.getInstance().addRunnable(() -> {
            synchronized (PVWidgetEditpartDelegate.this) {
                // Change the colours of all alarm sensitive components
                if (isBackColorAlarmSensitive) {
                    editpart.getFigure().setBackgroundColor(calculateBackColor());
                }
                if (isForeColorAlarmSensitive) {
                    editpart.getFigure().setForegroundColor(calculateForeColor());
                }
            }
        });
        scheduledFuture = ExecutionService.getInstance().getScheduledExecutorService().scheduleAtFixedRate(pulsingTask,
                PreferencesHelper.getGUIRefreshCycle(), PreferencesHelper.getGUIRefreshCycle(), TimeUnit.MILLISECONDS);
    }

    private void saveFigureOKStatus(IFigure figure) {
        saveForeColor = figure.getForegroundColor();
        saveBackColor = figure.getBackgroundColor();
    }

    /**
     * Start the updateSuppressTimer. All property change listeners of PV_Value property will temporarily removed until
     * timer is due.
     */
    protected synchronized void startUpdateSuppressTimer() {
        var pvValueProperty = editpart.getWidgetModel().getProperty(controlPVValuePropId);
        pvValueListeners = pvValueProperty.getAllPropertyChangeListeners();
        pvValueProperty.removeAllPropertyChangeListeners();
        updateSuppressTimer.start(timerTask, getUpdateSuppressTime());
    }

    public Border calculateBorder() {
        isBorderAlarmSensitive = getWidgetModel().isBorderAlarmSensitve();
        if (!isBorderAlarmSensitive) {
            return null;
        } else {
            Border alarmBorder;
            switch (alarmSeverity) {
            case NONE:
                if (editpart.getWidgetModel().getBorderStyle() == BorderStyle.NONE) {
                    alarmBorder = BORDER_NO_ALARM;
                } else {
                    alarmBorder = BorderFactory.createBorder(editpart.getWidgetModel().getBorderStyle(),
                            editpart.getWidgetModel().getBorderWidth(), editpart.getWidgetModel().getBorderColor(),
                            editpart.getWidgetModel().getName());
                }
                break;
            case MAJOR:
                alarmBorder = AlarmRepresentationScheme.getMajorBorder(editpart.getWidgetModel().getBorderStyle());
                break;
            case MINOR:
                alarmBorder = AlarmRepresentationScheme.getMinorBorder(editpart.getWidgetModel().getBorderStyle());
                break;
            case INVALID:
            case UNDEFINED:
            default:
                alarmBorder = AlarmRepresentationScheme.getInvalidBorder(editpart.getWidgetModel().getBorderStyle());
                break;
            }

            return alarmBorder;
        }
    }

    public Color calculateBackColor() {
        return calculateAlarmColor(isBackColorAlarmSensitive, saveBackColor);
    }

    public Color calculateForeColor() {
        return calculateAlarmColor(isForeColorAlarmSensitive, saveForeColor);
    }

    public Color calculateAlarmColor(boolean isSensitive, Color saveColor) {
        if (!isSensitive) {
            return saveColor;
        } else {
            var alarmColor = AlarmRepresentationScheme.getAlarmColor(alarmSeverity);
            if (alarmColor != null) {
                // Alarm severity is either "Major", "Minor" or "Invalid.
                if (isAlarmPulsing && (alarmSeverity == AlarmSeverity.MINOR || alarmSeverity == AlarmSeverity.MAJOR)) {
                    var alpha = 0.3;
                    int period;
                    if (alarmSeverity == AlarmSeverity.MINOR) {
                        period = PreferencesHelper.getPulsingAlarmMinorPeriod();
                    } else {
                        period = PreferencesHelper.getPulsingAlarmMajorPeriod();
                    }
                    alpha += Math.abs(System.currentTimeMillis() % period - period / 2) / (double) period;
                    alarmColor = new RGB((int) (saveColor.getRed() * alpha + alarmColor.red * (1 - alpha)),
                            (int) (saveColor.getGreen() * alpha + alarmColor.green * (1 - alpha)),
                            (int) (saveColor.getBlue() * alpha + alarmColor.blue * (1 - alpha)));
                }
                return CustomMediaFactory.getInstance().getColor(alarmColor);
            } else {
                // Alarm severity is "OK".
                return saveColor;
            }
        }
    }

    /**
     * Set PV to given value. Should accept Double, Double[], Integer, String, maybe more.
     *
     * @param pvPropId
     * @param value
     */
    @Override
    public void setPVValue(String pvPropId, Object value) {
        fireSetPVValue(pvPropId, value);
        var pv = pvMap.get(pvPropId);
        if (pv != null) {
            try {
                if (pvPropId.equals(controlPVPropId) && controlPVValuePropId != null && getUpdateSuppressTime() > 0) { // activate
                                                                                                                       // suppress
                                                                                                                       // timer
                    synchronized (this) {
                        if (updateSuppressTimer == null || timerTask == null) {
                            initUpdateSuppressTimer();
                        }
                        if (!updateSuppressTimer.isDue()) {
                            updateSuppressTimer.reset();
                        } else {
                            startUpdateSuppressTimer();
                        }
                    }

                }
                pv.setValue(value);
            } catch (Exception e) {
                UIBundlingThread.getInstance().addRunnable(() -> {
                    var message = "Failed to write PV:" + pv.getName();
                    ErrorHandlerUtil.handleError(message, e);
                });
            }
        }
    }

    public void setIgnoreOldPVValue(boolean ignoreOldValue) {
        ignoreOldPVValue = ignoreOldValue;
    }

    @Override
    public String[] getAllPVNames() {
        if (editpart.getWidgetModel().getPVMap().isEmpty()) {
            return new String[] { "" };
        }
        var result = new HashSet<String>();

        for (var sp : editpart.getWidgetModel().getPVMap().keySet()) {
            if (sp.isVisibleInPropSheet() && !sp.getPropertyValue().trim().isEmpty()) {
                result.add(sp.getPropertyValue());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public String getPVName() {
        if (getPV() != null) {
            return getPV().getName();
        }
        return getWidgetModel().getPVName();
    }

    @Override
    public void addSetPVValueListener(ISetPVValueListener listener) {
        if (setPVValueListeners == null) {
            setPVValueListeners = new ListenerList<>();
        }
        setPVValueListeners.add(listener);
    }

    protected void fireSetPVValue(String pvPropId, Object value) {
        if (setPVValueListeners == null) {
            return;
        }
        for (var listener : setPVValueListeners.getListeners()) {
            ((ISetPVValueListener) listener).beforeSetPVValue(pvPropId, value);
        }
    }

    private void updateWritable(AbstractWidgetModel widgetModel, IPV pv) {
        if (lastWriteAccess == null || lastWriteAccess.get() != pv.isWriteAllowed()) {
            if (lastWriteAccess == null) {
                lastWriteAccess = new AtomicBoolean();
            }
            lastWriteAccess.set(pv.isWriteAllowed());
            if (lastWriteAccess.get()) {
                UIBundlingThread.getInstance().addRunnable(editpart.getViewer().getControl().getDisplay(),
                        () -> setControlEnabled(true));
            } else {
                UIBundlingThread.getInstance().addRunnable(editpart.getViewer().getControl().getDisplay(),
                        () -> setControlEnabled(false));
            }
        }
    }

    /**
     * Set whether the editpart is enabled for PV control. Disabled editparts have greyed-out figures, and the cursor is
     * set to a cross.
     */
    @Override
    public void setControlEnabled(boolean enabled) {
        if (enabled) {
            var figure = editpart.getFigure();
            if (figure.getCursor() == Cursors.NO) {
                figure.setCursor(savedCursor);
            }
            figure.setEnabled(editpart.getWidgetModel().isEnabled());
            figure.repaint();
        } else {
            var figure = editpart.getFigure();
            if (figure.getCursor() != Cursors.NO) {
                savedCursor = figure.getCursor();
            }
            figure.setEnabled(false);
            figure.setCursor(Cursors.NO);
            figure.repaint();
        }
    }

    public void addAlarmSeverityListener(AlarmSeverityListener listener) {
        if (alarmSeverityListeners == null) {
            alarmSeverityListeners = new ListenerList<>();
        }
        alarmSeverityListeners.add(listener);
    }

    private void fireAlarmSeverityChanged(AlarmSeverity severity, IFigure figure) {
        if (alarmSeverityListeners == null) {
            return;
        }
        for (var listener : alarmSeverityListeners.getListeners()) {
            ((AlarmSeverityListener) listener).severityChanged(severity, figure);
        }
    }
}
