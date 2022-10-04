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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.commands.AddWidgetCommand;
import org.csstudio.opibuilder.commands.ChangeGuideCommand;
import org.csstudio.opibuilder.commands.CloneCommand;
import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.commands.WidgetCreateCommand;
import org.csstudio.opibuilder.commands.WidgetSetConstraintCommand;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.DisplayEditpart;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.csstudio.opibuilder.model.GuideModel;
import org.csstudio.opibuilder.model.IPVWidgetModel;
import org.csstudio.opibuilder.util.GuideUtil;
import org.csstudio.opibuilder.util.WidgetsService;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.handles.HandleBounds;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.rulers.RulerProvider;

/**
 * The EditPolicy for create/move/resize a widget.
 */
public class WidgetXYLayoutEditPolicy extends XYLayoutEditPolicy {

    @Override
    protected EditPolicy createChildEditPolicy(EditPart child) {
        var feedbackFactory = WidgetsService.getInstance()
                .getWidgetFeedbackFactory(((AbstractWidgetModel) child.getModel()).getTypeID());
        if (feedbackFactory != null && child instanceof AbstractBaseEditPart) {
            return new GraphicalFeedbackChildEditPolicy((AbstractBaseEditPart) child, feedbackFactory);
        } else {
            return new ResizableEditPolicy() {
                @Override
                protected List<?> createSelectionHandles() {
                    @SuppressWarnings("unchecked")
                    List<Handle> handleList = super.createSelectionHandles();
                    if (child.getModel() instanceof IPVWidgetModel && ((AbstractWidgetModel) (child.getModel()))
                            .getProperty(IPVWidgetModel.PROP_PVNAME).isVisibleInPropSheet()) {
                        handleList.add(new PVWidgetSelectionHandle((GraphicalEditPart) child));
                    }
                    return handleList;
                }
            };
        }
    }

    @Override
    protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
        if (!(child instanceof AbstractBaseEditPart) || !(constraint instanceof Rectangle)) {
            return super.createChangeConstraintCommand(request, child, constraint);
        }
        var part = (AbstractBaseEditPart) child;
        var widgetModel = part.getWidgetModel();

        var feedbackFactory = WidgetsService.getInstance().getWidgetFeedbackFactory(widgetModel.getTypeID());

        Command cmd = null;
        if (feedbackFactory != null) {
            cmd = feedbackFactory.createChangeBoundsCommand(widgetModel, request, (Rectangle) constraint);
        }
        if (cmd == null) {
            cmd = new WidgetSetConstraintCommand(widgetModel, request, (Rectangle) constraint);
        }

        List<ConnectionModel> allConnections = new ArrayList<>(
                part.getWidgetModel().getSourceConnections());
        allConnections.addAll(part.getWidgetModel().getTargetConnections());
        if (part.getWidgetModel() instanceof AbstractContainerModel) {
            for (var d : ((AbstractContainerModel) part.getWidgetModel()).getAllDescendants()) {
                allConnections.addAll(d.getSourceConnections());
                allConnections.addAll(d.getTargetConnections());
            }
        }
        if (allConnections.size() > 0) {
            var reRouteCmd = new CompoundCommand();
            for (var srcConn : allConnections) {
                reRouteCmd.add(new SetWidgetPropertyCommand(srcConn, ConnectionModel.PROP_POINTS, new PointList()));
            }
            cmd = cmd.chain(reRouteCmd);
        }
        // for guide support

        if ((request.getResizeDirection() & PositionConstants.NORTH_SOUTH) != 0) {
            var guidePos = (Integer) request.getExtendedData().get(SnapToGuides.KEY_HORIZONTAL_GUIDE);
            if (guidePos != null) {
                cmd = chainGuideAttachmentCommand(request, part, cmd, true);
            } else if (GuideUtil.getInstance().getGuide(widgetModel, true) != null) {
                // SnapToGuides didn't provide a horizontal guide, but
                // this part is attached
                // to a horizontal guide. Now we check to see if the
                // part is attached to
                // the guide along the edge being resized. If that is
                // the case, we need to
                // detach the part from the guide; otherwise, we leave
                // it alone.
                var alignment = GuideUtil.getInstance().getGuide(widgetModel, true).getAlignment(widgetModel);
                var edgeBeingResized = 0;
                if ((request.getResizeDirection() & PositionConstants.NORTH) != 0) {
                    edgeBeingResized = -1;
                } else {
                    edgeBeingResized = 1;
                }
                if (alignment == edgeBeingResized) {
                    cmd = cmd.chain(new ChangeGuideCommand(widgetModel, true));
                }
            }
        }

        if ((request.getResizeDirection() & PositionConstants.EAST_WEST) != 0) {
            var guidePos = (Integer) request.getExtendedData().get(SnapToGuides.KEY_VERTICAL_GUIDE);
            if (guidePos != null) {
                cmd = chainGuideAttachmentCommand(request, part, cmd, false);
            } else if (GuideUtil.getInstance().getGuide(widgetModel, false) != null) {
                var alignment = GuideUtil.getInstance().getGuide(widgetModel, false).getAlignment(widgetModel);
                var edgeBeingResized = 0;
                if ((request.getResizeDirection() & PositionConstants.WEST) != 0) {
                    edgeBeingResized = -1;
                } else {
                    edgeBeingResized = 1;
                }
                if (alignment == edgeBeingResized) {
                    cmd = cmd.chain(new ChangeGuideCommand(widgetModel, false));
                }
            }
        }

        if (request.getType().equals(REQ_MOVE_CHILDREN) || request.getType().equals(REQ_ALIGN_CHILDREN)) {
            cmd = chainGuideAttachmentCommand(request, part, cmd, true);
            cmd = chainGuideAttachmentCommand(request, part, cmd, false);
            cmd = chainGuideDetachmentCommand(request, part, cmd, true);
            cmd = chainGuideDetachmentCommand(request, part, cmd, false);
        }

        return cmd;
    }

    @Override
    protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Command createAddCommand(EditPart child, Object constraint) {
        if (!(child instanceof AbstractBaseEditPart) || !(constraint instanceof Rectangle)) {
            return super.createAddCommand(child, constraint);
        }

        var container = (AbstractContainerModel) getHost().getModel();
        var widget = (AbstractWidgetModel) child.getModel();
        var result = new CompoundCommand("Adding widgets to container");

        result.add(new AddWidgetCommand(container, widget, (Rectangle) constraint));
        return result;
    }

    @Override
    protected Command getCreateCommand(CreateRequest request) {
        var typeId = determineTypeIdFromRequest(request);

        var feedbackFactory = WidgetsService.getInstance().getWidgetFeedbackFactory(typeId);

        var widgetCreateCommand = createWidgetCreateCommand(request);
        if (widgetCreateCommand == null) {
            return null;
        }
        if (feedbackFactory != null) {
            var compoundCommand = new CompoundCommand();
            compoundCommand.add(widgetCreateCommand);
            var initialBoundsCommand = feedbackFactory.createInitialBoundsCommand(
                    (AbstractWidgetModel) request.getNewObject(), request, (Rectangle) getConstraintFor(request));
            if (initialBoundsCommand != null) {
                compoundCommand.add(initialBoundsCommand);
            }
            return compoundCommand;
        } else {
            return widgetCreateCommand;
        }
    }

    protected Command createWidgetCreateCommand(CreateRequest request) {
        var widgetCreateCommand = new WidgetCreateCommand((AbstractWidgetModel) request.getNewObject(),
                (AbstractContainerModel) getHost().getModel(), (Rectangle) getConstraintFor(request), false, true);
        return widgetCreateCommand;
    }

    /**
     * Override to provide custom feedback figure for the given create request.
     *
     * @param request
     *            the create request
     * @return custom feedback figure
     */
    @Override
    protected IFigure createSizeOnDropFeedback(CreateRequest request) {
        var typeId = determineTypeIdFromRequest(request);

        var feedbackFactory = WidgetsService.getInstance().getWidgetFeedbackFactory(typeId);

        if (feedbackFactory != null) {
            var feedbackFigure = feedbackFactory.createSizeOnDropFeedback(request);
            if (feedbackFigure != null) {
                addFeedback(feedbackFigure);
                return feedbackFigure;
            }
        }
        return super.createSizeOnDropFeedback(request);
    }

    @Override
    protected void showSizeOnDropFeedback(CreateRequest request) {
        var typeId = determineTypeIdFromRequest(request);

        var feedbackFactory = WidgetsService.getInstance().getWidgetFeedbackFactory(typeId);

        if (feedbackFactory != null) {
            var feedbackFigure = getSizeOnDropFeedback(request);

            feedbackFactory.showSizeOnDropFeedback(request, feedbackFigure, getCreationFeedbackOffset(request));

            // feedbackFigure.repaint();
        } else {
            super.showSizeOnDropFeedback(request);
        }
    }

    /**
     * Creates a prototype object to determine the type identification of the widget model, that is about to be created.
     *
     * @param request
     *            the create request
     * @return the type identification
     */
    @SuppressWarnings("rawtypes")
    private String determineTypeIdFromRequest(CreateRequest request) {
        var newObject = (Class) request.getNewObjectType();
        AbstractWidgetModel instance;
        var typeId = "";
        try {
            instance = (AbstractWidgetModel) newObject.newInstance();
            typeId = instance.getTypeID();
        } catch (Exception e) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "Unknown type", e);
        }

        return typeId;
    }

    /**
     * Adds a ChangeGuideCommand to the given Command.
     *
     * @param request
     *            The Request
     * @param part
     *            The AbstractWidgetEditPart, which model should be detached from a guide
     * @param cmd
     *            The Command
     * @param horizontal
     *            A boolean, true if the guide is horizontal, false otherwise
     * @return Command The given command
     */
    private Command chainGuideAttachmentCommand(Request request, AbstractBaseEditPart part, Command cmd,
            boolean horizontal) {
        var result = cmd;

        // Attach to guide, if one is given
        var guidePos = (Integer) request.getExtendedData()
                .get(horizontal ? SnapToGuides.KEY_HORIZONTAL_GUIDE : SnapToGuides.KEY_VERTICAL_GUIDE);
        if (guidePos != null) {
            var alignment = ((Integer) request.getExtendedData()
                    .get(horizontal ? SnapToGuides.KEY_HORIZONTAL_ANCHOR : SnapToGuides.KEY_VERTICAL_ANCHOR))
                    .intValue();
            var cgm = new ChangeGuideCommand(part.getWidgetModel(), horizontal);
            cgm.setNewGuide(findGuideAt(guidePos.intValue(), horizontal), alignment);
            result = result.chain(cgm);
        }

        return result;
    }

    /**
     * Adds a ChangeGuideCommand to the given Command.
     *
     * @param request
     *            The request
     * @param part
     *            The AbstractWidgetEditPart, which model should be detached from a guide
     * @param cmd
     *            The Command
     * @param horizontal
     *            A boolean, true if the guide is horizontal, false otherwise
     * @return Command The given command
     */
    private Command chainGuideDetachmentCommand(Request request, AbstractBaseEditPart part, Command cmd,
            boolean horizontal) {
        var result = cmd;

        // Detach from guide, if none is given
        var guidePos = (Integer) request.getExtendedData()
                .get(horizontal ? SnapToGuides.KEY_HORIZONTAL_GUIDE : SnapToGuides.KEY_VERTICAL_GUIDE);
        if (guidePos == null) {
            result = result.chain(new ChangeGuideCommand(part.getWidgetModel(), horizontal));
        }

        return result;
    }

    /**
     * Returns the guide at the given position and with the given orientation.
     *
     * @param pos
     *            The Position of the guide
     * @param horizontal
     *            The orientation of the guide
     * @return GuideModel The GuideModel
     */
    private GuideModel findGuideAt(int pos, boolean horizontal) {
        var provider = ((RulerProvider) getHost().getViewer().getProperty(
                horizontal ? RulerProvider.PROPERTY_VERTICAL_RULER : RulerProvider.PROPERTY_HORIZONTAL_RULER));
        return (GuideModel) provider.getGuideAt(pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Command getCloneCommand(ChangeBoundsRequest request) {
        var clone = new CloneCommand((AbstractContainerModel) getHost().getModel());

        for (var part : sortSelectedWidgets(request.getEditParts())) {
            clone.addPart((AbstractWidgetModel) part.getModel(), (Rectangle) getConstraintForClone(part, request));
        }

        // Attach to horizontal guide, if one is given
        var guidePos = (Integer) request.getExtendedData().get(SnapToGuides.KEY_HORIZONTAL_GUIDE);
        if (guidePos != null) {
            var hAlignment = ((Integer) request.getExtendedData().get(SnapToGuides.KEY_HORIZONTAL_ANCHOR)).intValue();
            clone.setGuide(findGuideAt(guidePos.intValue(), true), hAlignment, true);
        }

        // Attach to vertical guide, if one is given
        guidePos = (Integer) request.getExtendedData().get(SnapToGuides.KEY_VERTICAL_GUIDE);
        if (guidePos != null) {
            var vAlignment = ((Integer) request.getExtendedData().get(SnapToGuides.KEY_VERTICAL_ANCHOR)).intValue();
            clone.setGuide(findGuideAt(guidePos.intValue(), false), vAlignment, false);
        }
        return clone;
    }

    /**
     * Sort the selected widget as they were in their parents
     *
     * @return a list with all widget editpart that are currently selected
     */
    private final List<AbstractBaseEditPart> sortSelectedWidgets(List<?> selection) {
        List<AbstractBaseEditPart> sameParentWidgets = new ArrayList<>();
        List<AbstractBaseEditPart> differentParentWidgets = new ArrayList<>();
        List<AbstractBaseEditPart> result = new ArrayList<>();
        AbstractContainerModel parent = null;
        for (var o : selection) {
            if (o instanceof AbstractBaseEditPart && !(o instanceof DisplayEditpart)) {
                var widgetModel = ((AbstractBaseEditPart) o).getWidgetModel();
                if (parent == null) {
                    parent = widgetModel.getParent();
                }
                if (widgetModel.getParent() == parent) {
                    sameParentWidgets.add((AbstractBaseEditPart) o);
                } else {
                    differentParentWidgets.add((AbstractBaseEditPart) o);
                }
            }
        }
        // sort widgets to its original order
        if (sameParentWidgets.size() > 1) {
            var modelArray = sameParentWidgets.toArray(new AbstractBaseEditPart[0]);

            Arrays.sort(modelArray, (o1, o2) -> {
                if (o1.getWidgetModel().getParent().getChildren().indexOf(o1.getWidgetModel()) > o2.getWidgetModel()
                        .getParent().getChildren().indexOf(o2.getWidgetModel())) {
                    return 1;
                } else {
                    return -1;
                }
            });
            result.addAll(Arrays.asList(modelArray));
            if (differentParentWidgets.size() > 0) {
                result.addAll(differentParentWidgets);
            }
            return result;
        }
        if (differentParentWidgets.size() > 0) {
            sameParentWidgets.addAll(differentParentWidgets);
        }

        return sameParentWidgets;
    }

    // The minumum size should come from widget figure.
    @SuppressWarnings("deprecation")
    @Override
    protected Dimension getMinimumSizeFor(GraphicalEditPart child) {
        if (child instanceof AbstractBaseEditPart) {
            return ((AbstractBaseEditPart) child).getFigure().getMinimumSize();
        }
        return super.getMinimumSizeFor(child);
    }

    // This has been overriden to fix a bug when handle bounds does not equal with bounds. For example, polyline figue.
    @Override
    protected Command getResizeChildrenCommand(ChangeBoundsRequest request) {
        var resize = new CompoundCommand();
        Command c;
        GraphicalEditPart child;
        List<?> children = request.getEditParts();

        for (var i = 0; i < children.size(); i++) {
            child = (GraphicalEditPart) children.get(i);
            c = createChangeConstraintCommand(request, child,
                    translateToModelConstraint(getConstraintForResize(request, child)));
            resize.add(c);
        }
        return resize.unwrap();
    }

    // super.super.getConstraintFor() has been overriden to fix a bug when handle bounds does not equal with bounds. For
    // example, polyline figue.
    /**
     * Generates a draw2d constraint object derived from the specified child EditPart using the provided Request. The
     * returned constraint will be translated to the application's model later using
     * {@link #translateToModelConstraint(Object)}.
     *
     * @param request
     *            the ChangeBoundsRequest
     * @param child
     *            the child EditPart for which the constraint should be generated
     * @return the draw2d constraint
     */
    protected Object getConstraintForResize(ChangeBoundsRequest request, GraphicalEditPart child) {
        var bounds = child.getFigure().getBounds();
        if (child.getFigure() instanceof HandleBounds) {
            bounds = ((HandleBounds) child.getFigure()).getHandleBounds();
        }
        Rectangle rect = new PrecisionRectangle(bounds);
        child.getFigure().translateToAbsolute(rect);
        rect = request.getTransformedRectangle(rect);
        child.getFigure().translateToRelative(rect);
        rect.translate(getLayoutOrigin().getNegated());
        return getConstraintFor(rect);
    }
}
