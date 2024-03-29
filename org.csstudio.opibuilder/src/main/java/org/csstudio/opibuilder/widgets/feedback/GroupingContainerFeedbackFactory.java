/********************************************************************************
 * Copyright (c) 2013, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.feedback;

import java.util.Arrays;
import java.util.List;

import org.csstudio.opibuilder.feedback.DefaultGraphicalFeedbackFactory;
import org.csstudio.opibuilder.widgets.actions.LockUnlockChildrenAction;
import org.csstudio.opibuilder.widgets.editparts.GroupingContainerEditPart;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.Draw2dSingletonUtil;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;

/**
 * Feedback Factory for Grouping Contianer
 */
public class GroupingContainerFeedbackFactory extends DefaultGraphicalFeedbackFactory {

    @Override
    public List<Handle> createCustomHandles(GraphicalEditPart editPart) {

        if (editPart instanceof GroupingContainerEditPart) {
            Handle handle = new LockIndicatorHandle(editPart);
            return Arrays.asList(handle);
        }
        return super.createCustomHandles(editPart);
    }

    private final static class LockIndicatorHandle extends AbstractHandle {

        private static final Color handleBackColor = CustomMediaFactory.getInstance().getColor(255, 255, 150);
        private static final Color handleForeColor = CustomMediaFactory.getInstance().getColor(77, 77, 77);
        private static final String LOCKED = "Locked";
        private static final String UNLOCKED = "Unlocked";
        private Dimension textExtents;

        public LockIndicatorHandle(GraphicalEditPart owner) {
            super(owner, target -> {
                var ownerFigure = owner.getFigure();
                var preferedSize = target.getPreferredSize();
                var targetBounds = ownerFigure.getBounds().getCopy();
                ownerFigure.translateToAbsolute(targetBounds);
                target.translateToRelative(targetBounds);
                targetBounds.expand(preferedSize.height, preferedSize.height);
                target.setBounds(targetBounds);
            });
            setCursor(Cursors.HAND);
            setToolTip(new Label("Click to Lock/Unlock"));
        }

        private Dimension getTextExtent() {
            if (textExtents == null) {
                textExtents = Draw2dSingletonUtil.getTextUtilities().getTextExtents(
                        ((GroupingContainerEditPart) getOwner()).getWidgetModel().isLocked() ? LOCKED : UNLOCKED,
                        getFont());
            }
            return textExtents;
        }

        @Override
        protected DragTracker createDragTracker() {
            DragEditPartsTracker tracker = new DragEditPartsTracker(getOwner()) {
                @Override
                protected boolean handleButtonDown(int button) {
                    if ((button == 1 || button == 3) && getOwner() instanceof GroupingContainerEditPart) {
                        var activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                .getActivePart();

                        var commandStack = activePart.getAdapter(CommandStack.class);
                        if (commandStack != null) {
                            commandStack.execute(LockUnlockChildrenAction.createLockUnlockCommand(
                                    ((GroupingContainerEditPart) getOwner()).getWidgetModel()));
                        }
                    }
                    return true;
                }
            };
            tracker.setDefaultCursor(getCursor());
            return tracker;
        }

        @Override
        protected void paintFigure(Graphics graphics) {
            super.paintFigure(graphics);
            var locked = ((GroupingContainerEditPart) getOwner()).getWidgetModel().isLocked();
            graphics.setForegroundColor(handleForeColor);

            if (locked) {
                graphics.setLineStyle(SWT.LINE_DOT);
                graphics.drawRectangle(
                        getBounds().getCopy().shrink(getTextExtent().height - 3, getTextExtent().height - 3));
            }
            var textLocation = getTextLocation();
            graphics.setBackgroundColor(handleBackColor);
            graphics.setAlpha(180);
            graphics.fillRectangle(textLocation.x - 2, textLocation.y, getTextExtent().width + 4,
                    getTextExtent().height);
            graphics.setAlpha(250);
            graphics.drawText(locked ? LOCKED : UNLOCKED, textLocation);
        }

        private Point getTextLocation() {
            return getLocation().translate(getTextExtent().height, 0);
        }

        @Override
        public boolean containsPoint(int x, int y) {
            return Draw2dSingletonUtil.getRectangle().setBounds(getTextLocation(), getTextExtent()).contains(x, y);
        }

        @Override
        public Dimension getPreferredSize(int wHint, int hHint) {
            return getTextExtent();
        }
    }
}
