/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.feedback;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.feedback.IGraphicalFeedbackFactory;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.widgets.model.AbstractPolyModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

/**
 * Graphical feedback factory for polyline widgets.
 */
abstract class AbstractPolyFeedbackFactory implements IGraphicalFeedbackFactory {
    /**
     * An identifier which is used as key for extended data in request objects.
     */
    public static final String PROP_POINTS = "points";

    /**
     * Subclasses should return an appropriate feedback figure. This basically supports code inheritance for the
     * polyline and polygon implementations.
     *
     * @return a polyline or polygon figure which is used for graphical feedback
     */
    protected abstract Polyline createFeedbackFigure();

    @Override
    public final IFigure createDragSourceFeedbackFigure(AbstractWidgetModel model, Rectangle initalBounds) {
        assert model != null;
        assert model instanceof AbstractPolyModel : "model instanceof AbstractPolyModel";
        assert initalBounds != null;

        // get the points from the model
        var abstractPolyElement = (AbstractPolyModel) model;
        var points = abstractPolyElement.getPoints();

        // create feedbackfigure
        // RectangleWithPolyLineFigure r = new
        // RectangleWithPolyLineFigure(points);

        var feedbackFigure = new PolyFeedbackFigureWithRectangle(createFeedbackFigure(), points);

        return feedbackFigure;
    }

    @Override
    public void showChangeBoundsFeedback(AbstractWidgetModel model, PrecisionRectangle bounds, IFigure feedbackFigure,
            ChangeBoundsRequest request) {
        assert model != null;
        assert model instanceof AbstractPolyModel : "model instanceof AbstractPolyModel";
        assert bounds != null;
        assert feedbackFigure != null;
        assert feedbackFigure instanceof PolyFeedbackFigureWithRectangle
                : "feedbackFigure instanceof AbstractPolyFeedbackFigure";
        assert request != null;

        var figure = (PolyFeedbackFigureWithRectangle) feedbackFigure;

        figure.translateToRelative(bounds);

        // try to get a point list from the request (this happens only, when
        // poly point handles are dragged arround)
        var points = (PointList) request.getExtendedData().get(PROP_POINTS);

        // otherwise take the points from the model
        if (points == null) {
            points = ((AbstractPolyModel) model).getPoints();
        }

        // scale the points to the specified bounds
        var scaledPoints = PointListHelper.scaleTo(points.getCopy(), bounds);

        // apply the scaled points
        figure.setPoints(scaledPoints);
    }

    @Override
    public final Shape createSizeOnDropFeedback(CreateRequest createRequest) {
        assert createRequest != null;

        // Polyline polyline = new Polyline();

        // the request should contain a point list, because the creation is done
        // by a special creation tool
        var points = (PointList) createRequest.getExtendedData().get(PROP_POINTS);

        assert points != null;

        // polyline.setPoints(points);

        var feedbackFigure = createFeedbackFigure();
        feedbackFigure.setPoints(points);

        return feedbackFigure;
    }

    @Override
    public void showSizeOnDropFeedback(CreateRequest createRequest, IFigure feedbackFigure, Insets insets) {
        assert createRequest != null;
        assert feedbackFigure instanceof Polyline : "feedbackFigure instanceof Polyline";
        var polyline = (Polyline) feedbackFigure;

        // the request should contain a point list, because the creation is done
        // by a special creation tool
        var points = ((PointList) createRequest.getExtendedData().get(PROP_POINTS)).getCopy();

        assert points != null;

        // the points are viewer relative and need to be translated to reflect
        // the zoom level, scrollbar occurence etc.
        polyline.translateToRelative(points);

        polyline.setPoints(points);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final Class getCreationTool() {
        return PointListCreationTool.class;
    }

    @Override
    public final Command createInitialBoundsCommand(AbstractWidgetModel widgetModel, CreateRequest request,
            Rectangle bounds) {
        assert widgetModel instanceof AbstractPolyModel : "widgetModel instanceof AbstractPolyModel";
        assert request != null;
        assert bounds != null;

        var abstractPolyElement = (AbstractPolyModel) widgetModel;

        var points = (PointList) request.getExtendedData().get(PROP_POINTS);
        // necessary if the call was occurred by a "Drag and Drop" action
        if (points == null) {
            points = (PointList) widgetModel.getProperty(AbstractPolyModel.PROP_POINTS).getPropertyValue();
        }

        // the points are viewer relative and need to be translated to the
        // specified bounds, to reflect zoom level, scrollbar occurence etc.
        var scaledPoints = PointListHelper.scaleTo(points, bounds);

        return new ChangePolyPointsCommand(abstractPolyElement, scaledPoints);
    }

    @Override
    public final Command createChangeBoundsCommand(AbstractWidgetModel model, ChangeBoundsRequest request,
            Rectangle targetBounds) {
        assert model instanceof AbstractPolyModel : "model instanceof AbstractPolyModel";

        var correctedBounds = targetBounds;
        // if (model instanceof PolyLineModel) {
        // PolyLineModel polyline = (PolyLineModel) model;
        // int correctedX = targetBounds.x + (polyline.getLineWidth() / 2);
        // int correctedY = targetBounds.y + (polyline.getLineWidth() / 2);
        // correctedBounds = new Rectangle(correctedX, correctedY, targetBounds.width, targetBounds.height);
        // }

        var abstractPolyElement = (AbstractPolyModel) model;

        // try to get a point list from the request (this happens only, when
        // poly point handles are dragged arround)
        var points = (PointList) request.getExtendedData().get(PROP_POINTS);

        // otherwise take the points from the model
        if (points == null) {
            points = ((AbstractPolyModel) model).getPoints();
        }

        assert points != null;

        // the points are viewer relative and need to be translated to the
        // specified bounds, to reflect zoom level, scrollbar occurence etc.
        points = PointListHelper.scaleTo(points, correctedBounds);

        return new ChangePolyPointsCommand(abstractPolyElement, points);
    }

    @Override
    public final List<Handle> createCustomHandles(GraphicalEditPart hostEP) {
        assert hostEP != null;
        assert hostEP.getModel() instanceof AbstractPolyModel : "hostEP.getModel() instanceof AbstractPolyModel";

        // create some custom handles, which enable the user to drag arround
        // single points of the polyline
        List<Handle> handles = new ArrayList<>();

        var abstractPolyElement = (AbstractPolyModel) hostEP.getModel();

        var pointCount = abstractPolyElement.getPoints().size();

        for (var i = 0; i < pointCount; i++) {
            var myHandle = new PolyPointHandle(hostEP, i);
            handles.add(myHandle);
        }

        return handles;
    }
}
