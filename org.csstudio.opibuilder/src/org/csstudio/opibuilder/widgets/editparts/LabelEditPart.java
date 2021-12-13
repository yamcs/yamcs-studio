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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_ACTIONS;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_STYLE;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_WIDTH;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_FONT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_TOOLTIP;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_ALIGN_H;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_ALIGN_V;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_AUTOSIZE;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_SHOW_SCROLLBAR;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_TEXT;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_TRANSPARENT;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_WRAP_WORDS;

import org.csstudio.opibuilder.editparts.AbstractWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.LabelModel;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.csstudio.swt.widgets.figures.TextFigure;
import org.csstudio.swt.widgets.figures.TextFigure.H_ALIGN;
import org.csstudio.swt.widgets.figures.TextFigure.V_ALIGN;
import org.csstudio.swt.widgets.figures.WrappableTextFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.swt.widgets.Display;

/**
 * The editpart for Label widget.
 */
public class LabelEditPart extends AbstractWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var labelFigure = createTextFigure();
        labelFigure.setOpaque(!getWidgetModel().isTransparent());
        labelFigure.setHorizontalAlignment(getWidgetModel().getHorizontalAlignment());
        labelFigure.setVerticalAlignment(getWidgetModel().getVerticalAlignment());
        labelFigure.setSelectable(determinSelectable());
        labelFigure.setText(getWidgetModel().getText());
        labelFigure.setFontPixels(getWidgetModel().getFont().isSizeInPixels());
        if (labelFigure instanceof WrappableTextFigure) {
            ((WrappableTextFigure) labelFigure).setShowScrollbar(getWidgetModel().isShowScrollbar());
        }
        updatePropertyVisibility();
        return labelFigure;
    }

    protected TextFigure createTextFigure() {
        if (getWidgetModel().isWrapWords()) {
            return new WrappableTextFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
        }
        return new TextFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
    }

    @Override
    public void activate() {
        super.activate();
        if (getWidgetModel().isAutoSize()) {
            getWidgetModel().setSize(((TextFigure) figure).getAutoSizeDimension());
            figure.revalidate();
        }
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        if (getExecutionMode() == ExecutionMode.EDIT_MODE) {
            installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new TextDirectEditPolicy());
        }
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_TEXT, (oldValue, newValue, figure) -> {
            ((TextFigure) figure).setText((String) newValue);
            Display.getCurrent().timerExec(10, () -> {
                if (getWidgetModel().isAutoSize()) {
                    getWidgetModel().setSize(((TextFigure) figure).getAutoSizeDimension());
                }
            });

            return true;
        });

        setPropertyChangeHandler(PROP_ACTIONS, (oldValue, newValue, figure) -> {
            ((TextFigure) figure).setSelectable(determinSelectable());
            return false;
        });
        setPropertyChangeHandler(PROP_TOOLTIP, (oldValue, newValue, figure) -> {
            ((TextFigure) figure).setSelectable(determinSelectable());
            return false;
        });

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, figure) -> {
            Display.getCurrent().timerExec(10, () -> {
                if (getWidgetModel().isAutoSize()) {
                    getWidgetModel().setSize(((TextFigure) figure).getAutoSizeDimension());
                    figure.revalidate();
                }
            });

            return true;
        };
        setPropertyChangeHandler(PROP_FONT, handler);
        setPropertyChangeHandler(PROP_BORDER_STYLE, handler);
        setPropertyChangeHandler(PROP_BORDER_WIDTH, handler);

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, figure) -> {
            ((TextFigure) figure).setOpaque(!(Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_AUTOSIZE, (oldValue, newValue, figure) -> {
            if ((Boolean) newValue) {
                getWidgetModel().setSize(((TextFigure) figure).getAutoSizeDimension());
                figure.revalidate();
            }
            return true;
        });

        setPropertyChangeHandler(PROP_ALIGN_H, (oldValue, newValue, figure) -> {
            ((TextFigure) figure).setHorizontalAlignment(H_ALIGN.values()[(Integer) newValue]);
            return true;
        });

        setPropertyChangeHandler(PROP_ALIGN_V, (oldValue, newValue, figure) -> {
            ((TextFigure) figure).setVerticalAlignment(V_ALIGN.values()[(Integer) newValue]);
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
        getWidgetModel().getProperty(PROP_WRAP_WORDS)
                .addPropertyChangeListener(evt -> updatePropertyVisibility());

        setPropertyChangeHandler(PROP_SHOW_SCROLLBAR, (oldValue, newValue, figure) -> {
            if (figure instanceof WrappableTextFigure) {
                ((WrappableTextFigure) figure).setShowScrollbar((Boolean) newValue);
            }
            return false;
        });
    }

    private void updatePropertyVisibility() {
        getWidgetModel().setPropertyVisible(PROP_SHOW_SCROLLBAR, getWidgetModel().isWrapWords());
    }

    private void performDirectEdit() {
        new TextEditManager(this, new LabelCellEditorLocator((TextFigure) getFigure())).show();
    }

    @Override
    public void performRequest(Request request) {
        if (getExecutionMode() == ExecutionMode.EDIT_MODE && (request.getType() == RequestConstants.REQ_DIRECT_EDIT
                || request.getType() == RequestConstants.REQ_OPEN)) {
            performDirectEdit();
        }
    }

    @Override
    public LabelModel getWidgetModel() {
        return (LabelModel) getModel();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class key) {
        if (key == ITextFigure.class) {
            return ((TextFigure) getFigure());
        }

        return super.getAdapter(key);
    }

    private boolean determinSelectable() {
        return !getWidgetModel().getActionsInput().getActionsList().isEmpty()
                || getWidgetModel().getTooltip().trim().length() > 0;
    }
}
