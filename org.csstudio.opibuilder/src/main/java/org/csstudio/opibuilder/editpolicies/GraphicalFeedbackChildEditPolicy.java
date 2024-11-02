/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.editpolicies;

import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.feedback.IGraphicalFeedbackFactory;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.IPVWidgetModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * Provides support for selecting, positioning, and resizing an editpart. By default, selection is indicated via eight
 * square handles along the editpart's figure, and a rectangular handle that outlines the editpart with a 1-pixel black
 * line. The eight square handles will resize the current selection in the eight primary directions. The rectangular
 * handle will drag the current selection using a {@link org.eclipse.gef.tools.DragEditPartsTracker}.
 * <P>
 * By default, during feedback, a rectangle filled using XOR and outlined with dashes is drawn. This feedback can be
 * tailored by contributing a {@link IGraphicalFeedbackFactory} via the extension point
 * org.csstudio.sds.graphicalFeedbackFactories.
 */
public final class GraphicalFeedbackChildEditPolicy extends ResizableEditPolicy {
    /**
     * The edit part.
     */
    private final AbstractBaseEditPart _child;

    private final IGraphicalFeedbackFactory feedbackFactory;

    /**
     * Standard constructor.
     *
     * @param child
     *            An edit part.
     */
    protected GraphicalFeedbackChildEditPolicy(AbstractBaseEditPart child, IGraphicalFeedbackFactory feedbackFactory) {
        _child = child;
        this.feedbackFactory = feedbackFactory;
    }

    @Override
    protected IFigure createDragSourceFeedbackFigure() {

        var feedbackFigure = feedbackFactory.createDragSourceFeedbackFigure((AbstractWidgetModel) _child.getModel(),
                getInitialFeedbackBounds());
        if (feedbackFigure != null) {
            addFeedback(feedbackFigure);
            return feedbackFigure;
        }
        return super.createDragSourceFeedbackFigure();
    }

    /**
     * Shows or updates feedback for a change bounds request.
     *
     * @param request
     *            the request
     */
    @Override
    protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {

        var feedbackFigure = getDragSourceFeedbackFigure();

        var rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
        getHostFigure().translateToAbsolute(rect);

        var moveDelta = request.getMoveDelta();
        rect.translate(moveDelta);

        var sizeDelta = request.getSizeDelta();
        rect.resize(sizeDelta);

        feedbackFactory.showChangeBoundsFeedback((AbstractWidgetModel) getHost().getModel(), rect, feedbackFigure,
                request);

        feedbackFigure.repaint();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<? extends Handle> createSelectionHandles() {
        // get default handles
        List<Handle> handleList = (List<Handle>) super.createSelectionHandles();

        // add contributed handles

        var hostEP = (GraphicalEditPart) getHost();

        var contributedHandles = feedbackFactory.createCustomHandles(hostEP);

        if (contributedHandles != null) {
            handleList.addAll(contributedHandles);
        }

        if (hostEP.getModel() instanceof IPVWidgetModel && ((AbstractWidgetModel) (hostEP.getModel()))
                .getProperty(IPVWidgetModel.PROP_PVNAME).isVisibleInPropSheet()) {
            handleList.add(new PVWidgetSelectionHandle(hostEP));
        }
        return handleList;
    }
}
