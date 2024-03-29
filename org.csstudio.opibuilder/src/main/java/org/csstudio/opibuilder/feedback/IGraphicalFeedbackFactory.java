/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.feedback;

import java.util.List;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

/**
 * A graphical feedback factory is used to summarize and expose graphical feedback settings, which are normally spread
 * over different GEF EditPolicies.
 *
 * Contributors can implement their own factory to provide suitable graphical feedback for their widget models.
 */
public interface IGraphicalFeedbackFactory {
    /**
     * The returned figure will be used to provide graphical feedback during drag or resize operations.
     *
     * @param model
     *            the widget model
     * @param initalBounds
     *            the initial bounds
     * @return a customized feedback figure during draging, null if not necessary.
     */
    IFigure createDragSourceFeedbackFigure(AbstractWidgetModel model, Rectangle initalBounds);

    /**
     * This method will be called, when the size or location of the feedback changes during drag or resize operations.
     * The specified figure equals the figure, which is created by the
     * {@link #createDragSourceFeedbackFigure(AbstractWidgetModel, Rectangle)}.
     *
     * Implementors should apply the changed bounds to their individual feedback figure.
     *
     * @param widgetModel
     *            the corresponding widget model
     * @param bounds
     *            the new bounds
     * @param feedbackFigure
     *            the current feedback figure
     * @param request
     *            the change request
     */
    void showChangeBoundsFeedback(AbstractWidgetModel widgetModel, PrecisionRectangle bounds, IFigure feedbackFigure,
            ChangeBoundsRequest request);

    /**
     * The returned figure will be used to provide graphical feedback during the creation of new widget models.
     *
     * @param createRequest
     *            the create request
     *
     * @return a customized droop feedback figure, null if it is not necessary.
     */
    Shape createSizeOnDropFeedback(CreateRequest createRequest);

    /**
     * This method will be called, when the size or location of the feedback changes during the creation process.The
     * specified figure equals the figure, which is created by
     *
     * {@link #createSizeOnDropFeedback(CreateRequest)}.
     *
     * @param request
     *            the create request
     * @param feedbackFigure
     *            the current feedback figure
     * @param insets
     *            any insets that need to be applied to the creation feedback's bounds
     */
    void showSizeOnDropFeedback(CreateRequest request, IFigure feedbackFigure, Insets insets);

    /**
     * Gets the class type of a custom creation tool for the kind of objects, this factory is responsible for.
     *
     * @return the class type of a custom creation tool or null, if the default creation tool should be used
     */
    @SuppressWarnings("rawtypes")
    Class getCreationTool();

    /**
     * Returns a command, which handles the size and location changes, implied by the change bounds request and the
     * target bounds.
     *
     * @param widgetModel
     *            the affected widget model
     * @param request
     *            a change bounds request (may contain extended data, when a custom creation tool (see
     *            {@link #getCreationTool()}) or custom handles ({@link #createCustomHandles(GraphicalEditPart)}) are
     *            used.
     * @param targetBounds
     *            the new bounds
     * @return a undoable command, which handles the size and location changes, null if default change bounds command
     *         should be used.
     */
    Command createChangeBoundsCommand(AbstractWidgetModel widgetModel, ChangeBoundsRequest request,
            Rectangle targetBounds);

    /**
     * Returns a command, which handles the initial size and location during the creation of new widget models.
     *
     * @param widgetModel
     *            the created widget model
     * @param request
     *            the create request (may contain extended data, when a custom creation tool (see
     *            {@link #getCreationTool()}) is used
     * @param targetBounds
     *            the initial bounds
     * @return a undoable command, which sets up the initial size and location changes. null if it is not necessary to
     *         initialize size and location.
     */
    Command createInitialBoundsCommand(AbstractWidgetModel widgetModel, CreateRequest request, Rectangle targetBounds);

    /**
     * Creates and returns custom handles {@link Handle} for the specified edit part.
     *
     * @param editPart
     *            the edit part
     * @return a list, which contains all custom handles or null, if no custom handles should be created
     */
    List<Handle> createCustomHandles(GraphicalEditPart editPart);
}
