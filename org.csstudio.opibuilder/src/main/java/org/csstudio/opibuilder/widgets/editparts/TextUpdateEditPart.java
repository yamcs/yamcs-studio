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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_STYLE;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_WIDTH;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_FONT;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_ALIGN_H;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_ALIGN_V;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_AUTOSIZE;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_TEXT;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_TRANSPARENT;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_WRAP_WORDS;
import static org.csstudio.opibuilder.widgets.model.TextUpdateModel.PROP_FORMAT_TYPE;
import static org.csstudio.opibuilder.widgets.model.TextUpdateModel.PROP_PRECISION;
import static org.csstudio.opibuilder.widgets.model.TextUpdateModel.PROP_PRECISION_FROM_DB;
import static org.csstudio.opibuilder.widgets.model.TextUpdateModel.PROP_ROTATION;
import static org.csstudio.opibuilder.widgets.model.TextUpdateModel.PROP_SHOW_LOHI;
import static org.csstudio.opibuilder.widgets.model.TextUpdateModel.PROP_SHOW_UNITS;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.widgets.figures.NativeTextFigure;
import org.csstudio.opibuilder.widgets.model.TextUpdateModel;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.csstudio.swt.widgets.figures.TextFigure;
import org.csstudio.swt.widgets.figures.TextFigure.H_ALIGN;
import org.csstudio.swt.widgets.figures.TextFigure.V_ALIGN;
import org.csstudio.swt.widgets.figures.WrappableTextFigure;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.swt.widgets.Display;
import org.yamcs.studio.data.FormatEnum;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VType;
import org.yamcs.studio.data.vtype.ValueFactory;

public class TextUpdateEditPart extends AbstractPVWidgetEditPart {

    public static final String HEX_PREFIX = "0x";

    private TextUpdateModel widgetModel;
    private FormatEnum format;
    private boolean isAutoSize;
    private boolean isPrecisionFromDB;
    private boolean isShowUnits;
    private boolean isShowLoHi;
    private int precision;

    @Override
    protected IFigure doCreateFigure() {
        initFields();
        var labelFigure = createTextFigure();
        initTextFigure(labelFigure);
        return labelFigure;
    }

    protected void initTextFigure(TextFigure labelFigure) {
        labelFigure.setFont(widgetModel.getFont().getSWTFont());
        labelFigure.setFontPixels(getWidgetModel().getFont().isSizeInPixels());
        labelFigure.setOpaque(!widgetModel.isTransparent());
        labelFigure.setHorizontalAlignment(widgetModel.getHorizontalAlignment());
        labelFigure.setVerticalAlignment(widgetModel.getVerticalAlignment());
        labelFigure.setRotate(widgetModel.getRotationAngle());
    }

    protected void initFields() {
        // Initialize frequently used variables.
        widgetModel = getWidgetModel();
        format = widgetModel.getFormat();
        isAutoSize = widgetModel.isAutoSize();
        isPrecisionFromDB = widgetModel.isPrecisionFromDB();
        isShowUnits = widgetModel.isShowUnits();
        isShowLoHi = widgetModel.isShowLoHi();
        precision = widgetModel.getPrecision();
    }

    protected TextFigure createTextFigure() {
        if (getWidgetModel().isWrapWords()) {
            return new WrappableTextFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
        }
        return new TextFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        if (getExecutionMode() == ExecutionMode.EDIT_MODE) {
            installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new TextUpdateDirectEditPolicy());
        }
    }

    @Override
    public void activate() {
        super.activate();
        setFigureText(getWidgetModel().getText());
        if (getWidgetModel().isAutoSize()) {
            performAutoSize();
            figure.revalidate();
        }
    }

    /**
     * @param text
     */
    protected void setFigureText(String text) {
        if (getFigure() instanceof NativeTextFigure) {
            ((NativeTextFigure) getFigure()).getSWTWidget().setText(text);
        } else {
            ((TextFigure) getFigure()).setText(text);
        }
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_TEXT, (oldValue, newValue, figure) -> {
            setFigureText((String) newValue);

            if (isAutoSize) {
                Display.getCurrent().timerExec(10, this::performAutoSize);
            }
            return true;
        });

        setPropertyChangeHandler(PROP_FONT, (oldValue, newValue, figure) -> {
            figure.setFont(CustomMediaFactory.getInstance().getFont(((OPIFont) newValue).getFontData()));
            return true;
        });

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, figure) -> {
            Display.getCurrent().timerExec(10, () -> {
                if (getWidgetModel().isAutoSize()) {
                    performAutoSize();
                    figure.revalidate();
                }
            });

            return true;
        };
        setPropertyChangeHandler(PROP_FONT, handler);
        setPropertyChangeHandler(PROP_BORDER_STYLE, handler);
        setPropertyChangeHandler(PROP_BORDER_WIDTH, handler);

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, figure) -> {
            figure.setOpaque(!(Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_AUTOSIZE, (oldValue, newValue, figure) -> {
            isAutoSize = (Boolean) newValue;
            if ((Boolean) newValue) {
                performAutoSize();
                figure.revalidate();
            }
            return true;
        });

        setPropertyChangeHandler(PROP_ALIGN_H, (oldValue, newValue, figure) -> {
            if (figure instanceof TextFigure) {
                ((TextFigure) figure).setHorizontalAlignment(H_ALIGN.values()[(Integer) newValue]);
            }
            return true;
        });

        setPropertyChangeHandler(PROP_ALIGN_V, (oldValue, newValue, figure) -> {
            if (figure instanceof TextFigure) {
                ((TextFigure) figure).setVerticalAlignment(V_ALIGN.values()[(Integer) newValue]);
            }
            return true;
        });

        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, figure) -> {
            if (newValue == null) {
                return false;
            }
            formatValue(newValue, PROP_PVVALUE);
            return false;
        });

        setPropertyChangeHandler(PROP_FORMAT_TYPE, (oldValue, newValue, figure) -> {
            format = FormatEnum.values()[(Integer) newValue];
            formatValue(newValue, PROP_FORMAT_TYPE);
            return true;
        });

        setPropertyChangeHandler(PROP_PRECISION, (oldValue, newValue, figure) -> {
            precision = (Integer) newValue;
            formatValue(newValue, PROP_PRECISION);
            return true;
        });

        setPropertyChangeHandler(PROP_PRECISION_FROM_DB, (oldValue, newValue, figure) -> {
            isPrecisionFromDB = (Boolean) newValue;
            formatValue(newValue, PROP_PRECISION_FROM_DB);
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_UNITS, (oldValue, newValue, figure) -> {
            isShowUnits = (Boolean) newValue;
            formatValue(newValue, PROP_SHOW_UNITS);
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_LOHI, (oldValue, newValue, figure) -> {
            isShowLoHi = (Boolean) newValue;
            formatValue(newValue, PROP_SHOW_LOHI);
            return true;
        });

        setPropertyChangeHandler(PROP_ROTATION, (oldValue, newValue, figure) -> {
            if (figure instanceof TextFigure) {
                ((TextFigure) figure).setRotate((Double) newValue);
            }
            return true;
        });

        setPropertyChangeHandler(PROP_WRAP_WORDS, (oldValue, newValue, figure) -> {
            AbstractWidgetModel model = getWidgetModel();
            var parent = model.getParent();
            parent.removeChild(model);
            parent.addChild(model);
            parent.selectWidget(model, true);
            return false;
        });
    }

    @Override
    public TextUpdateModel getWidgetModel() {
        return (TextUpdateModel) getModel();
    }

    protected void performDirectEdit() {
        new TextEditManager(this, new LabelCellEditorLocator(getFigure())).show();
    }

    @Override
    public void performRequest(Request request) {
        if (getExecutionMode() == ExecutionMode.EDIT_MODE && (request.getType() == RequestConstants.REQ_DIRECT_EDIT
                || request.getType() == RequestConstants.REQ_OPEN)) {
            performDirectEdit();
        }
    }

    protected void performAutoSize() {
        if (figure instanceof TextFigure) {
            getWidgetModel().setSize(((TextFigure) getFigure()).getAutoSizeDimension());
        } else if (figure instanceof NativeTextFigure) {
            getWidgetModel().setSize(((NativeTextFigure) getFigure()).getAutoSizeDimension());
        }
    }

    protected String formatValue(Object newValue, String propId) {
        if (getExecutionMode() != ExecutionMode.RUN_MODE) {
            return null;
        }

        var tempPrecision = precision;
        if (isPrecisionFromDB) {
            tempPrecision = -1;
        }

        VType value = null;
        if (propId.equals(PROP_PVVALUE)) {
            value = (VType) newValue;
        } else {
            value = getPVValue(PROP_PVNAME);
        }

        var text = VTypeHelper.formatValue(format, value, tempPrecision);

        if (isShowUnits && VTypeHelper.getDisplayInfo(value) != null) {
            var units = VTypeHelper.getDisplayInfo(value).getUnits();
            if (units != null && units.trim().length() > 0) {
                text = text + " " + units;
            }
        }

        if (isShowLoHi) {
            text = text += VTypeHelper.getLoHiSuffix(value);
        }

        // synchronize the property value without fire listeners.
        widgetModel.getProperty(PROP_TEXT).setPropertyValue(text, false);
        setFigureText(text);

        if (isAutoSize) {
            performAutoSize();
        }

        return text;
    }

    @Override
    public String getValue() {
        if (getFigure() instanceof NativeTextFigure) {
            return ((NativeTextFigure) getFigure()).getText();
        }
        return ((TextFigure) getFigure()).getText();
    }

    @Override
    public void setValue(Object value) {
        String text;
        if (value instanceof Number) {
            text = formatValue(ValueFactory.newVDouble(((Number) value).doubleValue()), PROP_PVVALUE);
        } else {
            text = value.toString();
        }
        setFigureText(text);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class key) {
        if (key == ITextFigure.class) {
            return getFigure();
        }

        return super.getAdapter(key);
    }
}
