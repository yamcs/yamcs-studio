/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.commands;

import java.util.LinkedList;
import java.util.List;

import org.csstudio.opibuilder.actions.OPIWidgetsTransfer;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.model.GuideModel;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/**
 * A Command to clone the selected widgets.
 */
public final class CloneCommand extends Command {
    /**
     * The list of {@link AbstractWidgetModel}.
     */
    private List<AbstractWidgetModel> _models;
    /**
     * The list of cloned {@link AbstractWidgetModel}.
     */
    private List<AbstractWidgetModel> _clonedWidgets;
    /**
     * The parent for the AbstractWidgetModels.
     */
    private AbstractContainerModel _parent;
    /**
     * The horizontal Guide.
     */
    private GuideModel _hGuide;
    /**
     * The vertical Guide.
     */
    private GuideModel _vGuide;
    /**
     * The horizontal alignment.
     */
    private int _hAlignment;
    /**
     * The vertical alignment.
     */
    private int _vAlignment;
    /**
     * The difference between the original location and the new location.
     */
    private Dimension _difference;
    /**
     * The internal {@link CompoundCommand}.
     */
    private CompoundCommand _compoundCommand;

    /**
     * Constructor.
     * 
     * @param parent
     *            The parent {@link DisplayModel} for the widgets
     */
    public CloneCommand(AbstractContainerModel parent) {
        super("Clone Widgets");
        _models = new LinkedList<>();
        _parent = parent;
    }

    /**
     * Adds the given {@link AbstractWidgetModel} with the given {@link Rectangle} to this Command.
     * 
     * @param model
     *            The AbstractWidgetModel
     * @param newBounds
     *            The new bounds for the AbstractWidgetModel
     */
    public void addPart(AbstractWidgetModel model, Rectangle newBounds) {
        _models.add(model);
        _difference = calculateDifference(model, newBounds);
    }

    /**
     * Calculates the difference between the original location of the widget and the new location.
     * 
     * @param model
     *            The {@link AbstractWidgetModel}
     * @param newBounds
     *            The new bounds for the widget
     * @return Dimension The difference between the original location of the widget and the new location
     */
    private Dimension calculateDifference(AbstractWidgetModel model, Rectangle newBounds) {
        var dim = newBounds.getLocation().getDifference(model.getLocation());
        return dim;
    }

    /**
     * Sets the given {@link GuideModel} for the given orientation.
     * 
     * @param guide
     *            The guide
     * @param alignment
     *            The alignment for the guide
     * @param isHorizontal
     *            The orientation of the guide
     */
    public void setGuide(GuideModel guide, int alignment, boolean isHorizontal) {
        if (isHorizontal) {
            _hGuide = guide;
            _hAlignment = alignment;
        } else {
            _vGuide = guide;
            _vAlignment = alignment;
        }
    }

    /**
     * Returns a list with widget models that are currently stored on the clipboard.
     *
     * @return a list with widget models or an empty list
     */
    @SuppressWarnings("unchecked")
    private List<AbstractWidgetModel> getWidgetsFromClipboard() {
        var clipboard = new Clipboard(Display.getCurrent());
        var result = (List<AbstractWidgetModel>) clipboard.getContents(OPIWidgetsTransfer.getInstance());
        return result;
    }

    @Override
    public void execute() {
        var clipboard = new Clipboard(Display.getCurrent());
        var tempModel = new DisplayModel();

        for (var widget : _models) {
            tempModel.addChild(widget, false);
        }

        var xml = XMLUtil.widgetToXMLString(tempModel, false);
        clipboard.setContents(new Object[] { xml }, new Transfer[] { OPIWidgetsTransfer.getInstance() });

        _clonedWidgets = getWidgetsFromClipboard();

        _compoundCommand = new CompoundCommand();
        var i = 0;
        for (var widgetModel : _clonedWidgets) {
            if (_difference != null) {
                widgetModel.setLocation((widgetModel.getLocation().x + _difference.width),
                        (widgetModel.getLocation().y + _difference.height));
            } else {
                widgetModel.setLocation((widgetModel.getLocation().x + 10), (widgetModel.getLocation().y + 10));
            }
            _compoundCommand.add(new WidgetCreateCommand(widgetModel, _parent,
                    new Rectangle(widgetModel.getLocation(), widgetModel.getSize()), (i++ == 0 ? false : true)));

            if (_hGuide != null) {
                var hGuideCommand = new ChangeGuideCommand(widgetModel, true);
                hGuideCommand.setNewGuide(_hGuide, _hAlignment);
                _compoundCommand.add(hGuideCommand);
            }
            if (_vGuide != null) {
                var vGuideCommand = new ChangeGuideCommand(widgetModel, false);
                vGuideCommand.setNewGuide(_vGuide, _vAlignment);
                _compoundCommand.add(vGuideCommand);
            }
        }
        _compoundCommand.execute();
    }

    @Override
    public void redo() {
        _compoundCommand.redo();
    }

    @Override
    public void undo() {
        _compoundCommand.undo();
    }
}
