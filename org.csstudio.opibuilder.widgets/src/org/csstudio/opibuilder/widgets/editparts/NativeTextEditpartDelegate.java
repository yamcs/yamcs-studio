/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
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
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.figures.NativeTextFigure;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;

/**
 * The editpart delegate for native text input widget.
 */
public class NativeTextEditpartDelegate implements ITextInputEditPartDelegate {

    private TextInputEditpart editpart;
    private TextInputModel model;
    private Text text;
    private boolean skipTraverse;

    public NativeTextEditpartDelegate(TextInputEditpart editpart, TextInputModel model) {
        this.editpart = editpart;
        this.model = model;
    }

    @Override
    public IFigure doCreateFigure() {

        var style = SWT.NONE;
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

        var figure = new NativeTextFigure(editpart, style);
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
                    switch (model.getFocusTraverse()) {
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
                text.addTraverseListener(e -> {
                    // if key code is not tab, ignore
                    if (e.character != '\t' || skipTraverse) {
                        return;
                    }
                    e.doit = false;
                    skipTraverse = true;
                    if (e.stateMask == 0) {
                        text.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
                    } else {
                        text.traverse(SWT.TRAVERSE_TAB_NEXT);
                    }
                    skipTraverse = false;
                });
            }
            // Recover text if editing aborted.
            text.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent keyEvent) {
                    if (keyEvent.character == SWT.ESC) {
                        text.setText(model.getText());
                    }
                }
            });
            text.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (editpart.getPV() != null) {
                        text.setText(model.getText());
                    } else if (figure.isEnabled()) {
                        outputText(text.getText());
                    }
                }
            });
        }
        return figure;
    }

    protected void outputText(String newValue) {
        if (editpart.getPV() == null) {
            editpart.setPropertyValue(TextInputModel.PROP_TEXT, newValue);
            editpart.outputPVValue(newValue);
        } else {
            // PV may not be changed instantly, so recover it to old text first.
            text.setText(model.getText());
            // Write PV and update the text with new PV value if writing succeed.
            editpart.outputPVValue(newValue);
        }
    }

    @Override
    public void updatePropSheet() {
        var isMulti = model.isMultilineInput();
        model.setPropertyVisible(TextInputModel.PROP_SHOW_H_SCROLL, isMulti);
        model.setPropertyVisible(TextInputModel.PROP_SHOW_V_SCROLL, isMulti);
        model.setPropertyVisible(TextInputModel.PROP_WRAP_WORDS, isMulti);
        model.setPropertyVisible(TextInputModel.PROP_PASSWORD_INPUT, !isMulti);
    }

    @Override
    public void createEditPolicies() {
        if (editpart.getExecutionMode() == ExecutionMode.RUN_MODE) {
            editpart.installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, null);
        }
    }

    public void setFigureText(String text) {
        this.text.setText(text);
    }

    @Override
    public void registerPropertyChangeHandlers() {
        editpart.removeAllPropertyChangeHandlers(TextInputModel.PROP_ALIGN_H);

        PropertyChangeListener updatePropSheetListener = evt -> updatePropSheet();

        model.getProperty(TextInputModel.PROP_MULTILINE_INPUT).addPropertyChangeListener(updatePropSheetListener);

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, figure) -> {
            var parent = model.getParent();
            parent.removeChild(model);
            parent.addChild(model);
            parent.selectWidget(model, true);
            return false;
        };
        editpart.setPropertyChangeHandler(TextInputModel.PROP_SHOW_NATIVE_BORDER, handler);
        editpart.setPropertyChangeHandler(TextInputModel.PROP_MULTILINE_INPUT, handler);
        editpart.setPropertyChangeHandler(TextInputModel.PROP_WRAP_WORDS, handler);
        editpart.setPropertyChangeHandler(TextInputModel.PROP_SHOW_H_SCROLL, handler);
        editpart.setPropertyChangeHandler(TextInputModel.PROP_SHOW_V_SCROLL, handler);
        editpart.setPropertyChangeHandler(TextInputModel.PROP_PASSWORD_INPUT, handler);
        editpart.setPropertyChangeHandler(TextInputModel.PROP_ALIGN_H, handler);

    }

    public void performAutoSize() {
        model.setSize(((NativeTextFigure) editpart.getFigure()).getAutoSizeDimension());
    }

    public String getValue() {
        return text.getText();
    }

}
