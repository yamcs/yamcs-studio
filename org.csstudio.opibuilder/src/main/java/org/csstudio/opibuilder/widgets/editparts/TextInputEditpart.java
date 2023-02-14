/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_TEXT;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_TRANSPARENT;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_WRAP_WORDS;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_LIMITS_FROM_PV;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_MAX;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_MIN;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_NEXT_FOCUS;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_PASSWORD_INPUT;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_READ_ONLY;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_SELECTOR_TYPE;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_SHOW_H_SCROLL;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_SHOW_NATIVE_BORDER;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_SHOW_V_SCROLL;
import static org.csstudio.opibuilder.widgets.model.TextInputModel.PROP_STYLE;
import static org.csstudio.opibuilder.widgets.model.TextUpdateModel.PROP_ROTATION;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.widgets.model.ActionButtonModel.Style;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.csstudio.swt.widgets.figures.TextFigure;
import org.csstudio.swt.widgets.figures.TextInputFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.FormatEnum;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.Array;
import org.yamcs.studio.data.vtype.Scalar;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.VType;
import org.yamcs.studio.script.GUIUtil;

public class TextInputEditpart extends TextUpdateEditPart {

    private static final char SPACE = ' ';
    private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat();
    private IPVListener pvLoadLimitsListener;
    private org.yamcs.studio.data.vtype.Display meta = null;

    private ITextInputEditPartDelegate delegate;

    @Override
    public TextInputModel getWidgetModel() {
        return (TextInputModel) getModel();
    }

    protected void setDelegate(ITextInputEditPartDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected IFigure doCreateFigure() {
        initFields();

        if (shouldBeTextInputFigure()) {
            var textInputFigure = (TextInputFigure) createTextFigure();
            initTextFigure(textInputFigure);
            setDelegate(new Draw2DTextInputEditpartDelegate(this, getWidgetModel(), textInputFigure));
        } else {
            setDelegate(new NativeTextEditpartDelegate(this, getWidgetModel()));
        }

        getPVWidgetEditpartDelegate().setUpdateSuppressTime(-1);
        updatePropSheet();

        return delegate.doCreateFigure();
    }

    /**
     * @return true if it should use Draw2D {@link TextInputFigure}.
     */
    protected boolean shouldBeTextInputFigure() {
        var model = getWidgetModel();
        return model.getStyle() != Style.NATIVE;
    }

    @Override
    protected TextFigure createTextFigure() {
        return new TextInputFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        delegate.createEditPolicies();
    }

    @Override
    public void activate() {
        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);
        super.activate();
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
            var model = getWidgetModel();
            if (model.isLimitsFromPV()) {
                var pv = getPV(PROP_PVNAME);
                if (pv != null) {
                    if (pvLoadLimitsListener == null) {
                        pvLoadLimitsListener = new IPVListener() {
                            @Override
                            public void valueChanged(IPV pv) {
                                var value = pv.getValue();
                                if (value != null && VTypeHelper.getDisplayInfo(value) != null) {
                                    var new_meta = VTypeHelper.getDisplayInfo(value);
                                    if (meta == null || !meta.equals(new_meta)) {
                                        meta = new_meta;
                                        // Update min/max from the control range of the PV
                                        model.setPropertyValue(PROP_MAX, meta.getUpperCtrlLimit());
                                        model.setPropertyValue(PROP_MIN, meta.getLowerCtrlLimit());
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

    public void outputPVValue(String text) {
        if (!getWidgetModel().getConfirmMessage().isEmpty()) {
            if (!GUIUtil.openConfirmDialog("PV Name: " + getPVName() + "\nNew Value: " + text + "\n\n"
                    + getWidgetModel().getConfirmMessage())) {
                return;
            }
        }
        try {
            Object result;
            if (getWidgetModel().getFormat() != FormatEnum.STRING && text.trim().indexOf(SPACE) != -1) {
                result = parseStringArray(text);
            } else {
                result = parseString(text);
            }
            setPVValue(PROP_PVNAME, result);
        } catch (Exception e) {
            var msg = NLS.bind("Failed to write value to PV {0} from widget {1}.\nIllegal input : {2} \n",
                    new String[] { getPVName(), getWidgetModel().getName(), text }) + e.toString();
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, msg);
        }
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            removeAllPropertyChangeHandlers(PROP_TEXT);
            setPropertyChangeHandler(PROP_TEXT, (oldValue, newValue, figure) -> {
                var text = (String) newValue;

                if (getPV() == null) {
                    setFigureText(text);
                    if (getWidgetModel().isAutoSize()) {
                        Display.getCurrent().timerExec(10, this::performAutoSize);
                    }
                }
                // Output pv value even if pv name is empty, so setPVValuelistener can be triggered.
                outputPVValue(text);
                return false;
            });
        }

        setPropertyChangeHandler(PROP_PVNAME, (oldValue, newValue, figure) -> {
            registerLoadLimitsListener();
            return false;
        });

        getWidgetModel().getProperty(PROP_LIMITS_FROM_PV).addPropertyChangeListener(evt -> updatePropSheet());
        getWidgetModel().getProperty(PROP_SELECTOR_TYPE).addPropertyChangeListener(evt -> updatePropSheet());

        getWidgetModel().getProperty(PROP_STYLE).addPropertyChangeListener(evt -> reCreateWidget());

        delegate.registerPropertyChangeHandlers();
    }

    private void reCreateWidget() {
        var model = getWidgetModel();
        var parent = model.getParent();
        parent.removeChild(model);
        parent.addChild(model);
        parent.selectWidget(model, true);
    }

    @Override
    public DragTracker getDragTracker(Request request) {
        if (getExecutionMode() == ExecutionMode.RUN_MODE && delegate instanceof Draw2DTextInputEditpartDelegate) {
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
        if (getFigure().isEnabled() && ((request.getType() == RequestConstants.REQ_DIRECT_EDIT
                && getExecutionMode() != ExecutionMode.RUN_MODE) || request.getType() == RequestConstants.REQ_OPEN)) {
            performDirectEdit();
        }
    }

    @Override
    protected void performDirectEdit() {
        new TextEditManager(this, new LabelCellEditorLocator(getFigure()), getWidgetModel().isMultilineInput()).show();
    }

    /**
     * If the text has spaces in the string and the PV is numeric array type, it will return an array of numeric values.
     *
     * @param text
     * @return
     * @throws ParseException
     */
    private Object parseStringArray(String text) throws ParseException {
        var texts = text.split(" +");
        var pvValue = getPVValue(PROP_PVNAME);
        if (pvValue instanceof VNumberArray) {
            var result = new double[texts.length];
            for (var i = 0; i < texts.length; i++) {
                var o = parseString(texts[i]);
                if (o instanceof Number) {
                    result[i] = ((Number) o).doubleValue();
                } else {
                    throw new ParseException(texts[i] + " cannot be parsed as a number!", i);
                }
            }
            return result;
        }

        return parseString(text);
    }

    /**
     * Parse string to a value according PV value type and format
     *
     * @param text
     * @return value
     * @throws ParseException
     */
    private Object parseString(String text) throws ParseException {
        var pvValue = getPVValue(PROP_PVNAME);
        var formatEnum = getWidgetModel().getFormat();

        if (pvValue == null) {
            return text;
        }

        return parseStringForPVManagerPV(formatEnum, text, pvValue);
    }

    private Object parseStringForPVManagerPV(FormatEnum formatEnum, String text, VType pvValue) throws ParseException {
        if (pvValue instanceof Scalar) {
            var value = ((Scalar) pvValue).getValue();
            if (value instanceof Number) {
                switch (formatEnum) {
                case HEX:
                case HEX64:
                    return parseHEX(text, true);
                case STRING:
                    return text;
                case DECIMAL:
                case COMPACT:
                case EXP:
                    return parseDouble(text, true);
                case SEXA:
                    return parseSexagesimal(text, true);
                case SEXA_HMS:
                    return (parseSexagesimal(text, true) * Math.PI / 12.0);
                case SEXA_DMS:
                    return (parseSexagesimal(text, true) * Math.PI / 180.0);
                case TIME:
                    return parseTime(text);
                case DEFAULT:
                default:
                    try {
                        return parseDouble(text, true);
                    } catch (ParseException e) {
                        return text;
                    }
                }
            } else if (value instanceof String) {
                if (pvValue instanceof VEnum) {
                    switch (formatEnum) {
                    case HEX:
                    case HEX64:
                        return parseHEX(text, true);
                    case STRING:
                        return text;
                    case DECIMAL:
                    case EXP:
                    case COMPACT:
                        return parseDouble(text, true);
                    case SEXA:
                        return parseSexagesimal(text, true);
                    case SEXA_HMS:
                        return (parseSexagesimal(text, true) * Math.PI / 12.0);
                    case SEXA_DMS:
                        return (parseSexagesimal(text, true) * Math.PI / 180.0);
                    case TIME:
                        return parseTime(text);
                    case DEFAULT:
                    default:
                        try {
                            return parseDouble(text, true);
                        } catch (ParseException e) {
                            return text;
                        }
                    }
                } else {
                    return text;
                }
            }
        } else if (pvValue instanceof Array) {
            if (pvValue instanceof VNumberArray) {
                switch (formatEnum) {
                case HEX:
                case HEX64:
                    return parseHEX(text, true);
                case STRING:
                    return parseCharArray(text, ((VNumberArray) pvValue).getData().size());
                case DECIMAL:
                case EXP:
                case COMPACT:
                    return parseDouble(text, true);
                case SEXA:
                    return parseSexagesimal(text, true);
                case SEXA_HMS:
                    return (parseSexagesimal(text, true) * Math.PI / 12.0);
                case SEXA_DMS:
                    return (parseSexagesimal(text, true) * Math.PI / 180.0);
                case DEFAULT:
                default:
                    try {
                        return parseDouble(text, true);
                    } catch (ParseException e) {
                        return text;
                    }
                }
            } else {
                return text;
            }
        }
        return text;
    }

    private int[] parseCharArray(String text, int currentLength) {
        // Turn text into array of character codes,
        // at least as long as the current value of the PV.
        // New text may be longer (and IOC can then refuse the extra chars)
        var newLength = text.length();
        var iString = new int[Math.max(newLength, currentLength)];
        var textChars = text.toCharArray();

        for (var ii = 0; ii < newLength; ii++) {
            iString[ii] = Integer.valueOf(textChars[ii]);
        }
        for (var ii = newLength; ii < currentLength; ii++) {
            iString[ii] = 0;
        }
        return iString;
    }

    private double parseDouble(String text, boolean coerce) throws ParseException {
        if (text.contains("\n") && !text.endsWith("\n")) {
            throw new ParseException(NLS.bind("{0} cannot be parsed to double", text), text.indexOf("\n"));
        }
        var value = DECIMAL_FORMAT.parse(text.replace('e', 'E')).doubleValue();
        if (coerce) {
            double min = getWidgetModel().getMinimum();
            double max = getWidgetModel().getMaximum();
            // Only apply sensible limits, not min==max==0
            if (min < max) {
                if (value < min) {
                    value = min;
                } else if (value > max) {
                    value = max;
                }
            }
        }
        return value;
    }

    private int parseHEX(String text, boolean coerce) {
        var valueText = text.trim();
        if (text.startsWith(TextUpdateEditPart.HEX_PREFIX)) {
            valueText = text.substring(TextUpdateEditPart.HEX_PREFIX.length());
        }
        if (valueText.contains(" ")) {
            valueText = valueText.substring(0, valueText.indexOf(SPACE));
        }
        var i = Long.parseLong(valueText, 16);
        if (coerce) {
            double min = getWidgetModel().getMinimum();
            double max = getWidgetModel().getMaximum();
            // Only apply sensible limits, not min==max==0
            if (min < max) {
                if (i < min) {
                    i = (long) min;
                } else if (i > max) {
                    i = (long) max;
                }
            }
        }
        return (int) i; // EPICS_V3_PV doesn't support Long
    }

    private long parseTime(String text) throws ParseException {
        return YamcsPlugin.getDefault().parseTime(text).toEpochMilli();
    }

    private double parseSexagesimal(String text, boolean coerce) throws ParseException {
        var value = Double.NaN;
        var negative = false;
        var hours = 0.0;
        var minutes = 0.0;
        var seconds = 0.0;
        var error = false;

        var parts = text.trim().split(":");

        if (parts.length > 0) {
            if (parts[0].trim().startsWith("-")) {
                negative = true;
                parts[0] = parts[0].replace("-", "");
            }

            /* Hours are always present */
            try {
                hours = parseDouble(parts[0], false);
            } catch (NumberFormatException e) {
            }

            if ((hours == Math.floor(hours)) && !(hours < 0.0)) {
                value = hours;

                /* Minutes are present */
                if (parts.length > 1) {
                    try {
                        minutes = parseDouble(parts[1], false);
                    } catch (NumberFormatException e) {
                    }

                    if ((minutes == Math.floor(minutes)) && !(minutes < 0.0)) {
                        value += minutes / 60.0;

                        /* Seconds are present */
                        if (parts.length > 2) {
                            try {
                                seconds = parseDouble(parts[2], false);
                            } catch (NumberFormatException e) {
                            }

                            if (!(seconds < 0.0)) {
                                value += seconds / 3600.0;
                            } else {
                                error = true;
                            }
                        }
                    } else {
                        error = true;
                    }
                }
            } else {
                error = true;
            }
        } else {
            error = true;
        }

        if (error) {
            value = Double.NaN;
            throw new ParseException(NLS.bind("{0} cannot be parsed to double", text), text.indexOf("\n"));
        } else {
            /* Apply original sign */
            if (negative) {
                value = -value;
            }

            if (coerce) {
                double min = getWidgetModel().getMinimum();
                double max = getWidgetModel().getMaximum();
                // Only apply sensible limits, not min==max==0
                if (min < max) {
                    if (value < min) {
                        value = min;
                    } else if (value > max) {
                        value = max;
                    }
                }
            }
        }

        return value;
    }

    @Override
    protected String formatValue(Object newValue, String propId) {
        var text = super.formatValue(newValue, propId);
        getWidgetModel().setPropertyValue(PROP_TEXT, text, false);
        return text;
    }

    protected void updatePropSheet() {
        var model = getWidgetModel();
        model.setPropertyVisible(PROP_MAX, !getWidgetModel().isLimitsFromPV());
        model.setPropertyVisible(PROP_MIN, !getWidgetModel().isLimitsFromPV());

        // set native text related properties visibility
        var isNative = delegate instanceof NativeTextEditpartDelegate;
        model.setPropertyVisible(PROP_SHOW_NATIVE_BORDER, isNative);
        model.setPropertyVisible(PROP_PASSWORD_INPUT, isNative);
        model.setPropertyVisible(PROP_READ_ONLY, isNative);
        model.setPropertyVisible(PROP_SHOW_H_SCROLL, isNative);
        model.setPropertyVisible(PROP_SHOW_V_SCROLL, isNative);
        model.setPropertyVisible(PROP_NEXT_FOCUS, isNative);
        model.setPropertyVisible(PROP_WRAP_WORDS, isNative);

        // set classic text figure related properties visibility
        model.setPropertyVisible(PROP_TRANSPARENT, !isNative);
        model.setPropertyVisible(PROP_ROTATION, !isNative);
        model.setPropertyVisible(PROP_SELECTOR_TYPE, !isNative);

        delegate.updatePropSheet();
    }

    @Override
    protected void setFigureText(String text) {
        if (delegate instanceof NativeTextEditpartDelegate) {
            ((NativeTextEditpartDelegate) delegate).setFigureText(text);
        } else {
            super.setFigureText(text);
        }
    }

    @Override
    protected void performAutoSize() {
        if (delegate instanceof NativeTextEditpartDelegate) {
            ((NativeTextEditpartDelegate) delegate).performAutoSize();
        } else {
            super.performAutoSize();
        }
    }

    @Override
    public String getValue() {
        if (delegate instanceof NativeTextEditpartDelegate) {
            return ((NativeTextEditpartDelegate) delegate).getValue();
        } else {
            return super.getValue();
        }
    }
}
