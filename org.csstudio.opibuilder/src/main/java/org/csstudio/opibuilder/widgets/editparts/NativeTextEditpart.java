/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import java.beans.PropertyChangeListener;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.figures.NativeTextFigure;
import org.csstudio.opibuilder.widgets.model.NativeTextModel;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;

/**
 * The editpart for native text widget.
 *
 * @deprecated not used anymore
 */
@Deprecated
public class NativeTextEditpart extends TextInputEditpart {

    private Text text;

    @Override
    public NativeTextModel getWidgetModel() {
        return (NativeTextModel) getModel();
    }

    @Override
    protected IFigure doCreateFigure() {
        initFields();

        var style = SWT.NONE;
        var model = getWidgetModel();
        if (model.isShowNativeBorder()) {
            style |= SWT.BORDER;
        }
        if (model.isMultilineInput()) {
            style |= SWT.MULTI;
            if (model.isShowHScroll()) {
                style |= SWT.H_SCROLL;
            }
            if (model.isShowVScroll()) {
                style |= SWT.V_SCROLL;
            }
            if (model.isWrapWords()) {
                style |= SWT.WRAP;
            }
        } else {
            style |= SWT.SINGLE;
            if (model.isPasswordInput()) {
                style |= SWT.PASSWORD;
            }
        }
        if (model.isReadOnly()) {
            style |= SWT.READ_ONLY;
        }
        switch (model.getHorizontalAlignment()) {
        case CENTER:
            style |= SWT.CENTER;
            break;
        case LEFT:
            style |= SWT.LEFT;
            break;
        case RIGHT:
            style |= SWT.RIGHT;
        default:
            break;
        }

        var figure = new NativeTextFigure(this, style);
        text = figure.getSWTWidget();

        if (!model.isReadOnly()) {
            if (model.isMultilineInput()) {
                text.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent keyEvent) {
                        if (keyEvent.character == '\r') { // Return key
                            if (text != null && !text.isDisposed() && (text.getStyle() & SWT.MULTI) != 0) {
                                if ((keyEvent.stateMask & SWT.CTRL) != 0) {
                                    outputText(text.getText());
                                    keyEvent.doit = false;
                                    text.getShell().setFocus();
                                }
                            }

                        }
                    }
                });
            } else {
                text.addListener(SWT.DefaultSelection, e -> {
                    outputText(text.getText());
                    switch (getWidgetModel().getFocusTraverse()) {
                    case LOSE:
                        text.getShell().setFocus();
                        break;
                    case NEXT:
                        text.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
                        break;
                    case PREVIOUS:
                        text.traverse(SWT.TRAVERSE_TAB_NEXT);
                        break;
                    case KEEP:
                    default:
                        break;
                    }
                });
            }
            // Recover text if editing aborted.
            text.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent keyEvent) {
                    if (keyEvent.character == SWT.ESC) {
                        text.setText(getWidgetModel().getText());
                    }
                }
            });
            text.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (getPV() != null) {
                        text.setText(getWidgetModel().getText());
                    } else if (figure.isEnabled()) {
                        outputText(text.getText());
                    }
                }
            });
        }

        getPVWidgetEditpartDelegate().setUpdateSuppressTime(-1);
        updatePropSheet();
        return figure;
    }

    protected void outputText(String newValue) {
        if (getPV() == null) {
            setPropertyValue(NativeTextModel.PROP_TEXT, newValue);
            outputPVValue(newValue);
        } else {
            // PV may not be changed instantly, so recover it to old text first.
            text.setText(getWidgetModel().getText());
            // Write PV and update the text with new PV value if writing succeed.
            outputPVValue(newValue);
        }
    }

    @Override
    protected void updatePropSheet() {
        super.updatePropSheet();
        var isMulti = getWidgetModel().isMultilineInput();
        getWidgetModel().setPropertyVisible(NativeTextModel.PROP_SHOW_H_SCROLL, isMulti);
        getWidgetModel().setPropertyVisible(NativeTextModel.PROP_SHOW_V_SCROLL, isMulti);
        getWidgetModel().setPropertyVisible(NativeTextModel.PROP_WRAP_WORDS, isMulti);
        getWidgetModel().setPropertyVisible(NativeTextModel.PROP_PASSWORD_INPUT, !isMulti);
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, null);
        }
    }

    @Override
    protected void setFigureText(String text) {
        this.text.setText(text);
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();
        removeAllPropertyChangeHandlers(NativeTextModel.PROP_ALIGN_H);

        PropertyChangeListener updatePropSheetListener = evt -> updatePropSheet();

        getWidgetModel().getProperty(NativeTextModel.PROP_MULTILINE_INPUT)
                .addPropertyChangeListener(updatePropSheetListener);

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, figure) -> {
            AbstractWidgetModel model = getWidgetModel();
            var parent = model.getParent();
            parent.removeChild(model);
            parent.addChild(model);
            parent.selectWidget(model, true);
            return false;
        };
        setPropertyChangeHandler(NativeTextModel.PROP_SHOW_NATIVE_BORDER, handler);
        setPropertyChangeHandler(NativeTextModel.PROP_MULTILINE_INPUT, handler);
        setPropertyChangeHandler(NativeTextModel.PROP_WRAP_WORDS, handler);
        setPropertyChangeHandler(NativeTextModel.PROP_SHOW_H_SCROLL, handler);
        setPropertyChangeHandler(NativeTextModel.PROP_SHOW_V_SCROLL, handler);
        setPropertyChangeHandler(NativeTextModel.PROP_PASSWORD_INPUT, handler);
        setPropertyChangeHandler(NativeTextModel.PROP_ALIGN_H, handler);
    }

    @Override
    protected String formatValue(Object newValue, String propId) {
        var text = super.formatValue(newValue, propId);
        getWidgetModel().setPropertyValue(TextInputModel.PROP_TEXT, text, false);
        return text;
    }

    @Override
    protected void performAutoSize() {
        getWidgetModel().setSize(((NativeTextFigure) getFigure()).getAutoSizeDimension());
    }

    @Override
    public String getValue() {
        return text.getText();
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
