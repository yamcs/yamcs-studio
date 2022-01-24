/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The actions to distribute widgets.
 */
public class DistributeWidgetsAction extends SelectionAction {

    public enum DistributeType {
        HORIZONTAL_GAP("Distribute by Horizontal GAP", "icons/distribute_hg.png"),
        HORIZONTAL_CENTERS("Distribute by Horizontal Centers", "icons/distribute_hc.png"),
        HORIZONTAL_COMPRESS("Distribute by Horizontal Compress", "icons/distribute_hcompress.png"),
        VERTICAL_GAP("Distribute by Vertical GAP", "icons/distribute_vg.png"),
        VERTICAL_CENTERS("Distribute by Vertical Centers", "icons/distribute_vc.png"),
        VERTICAL_COMPRESS("Distribute by Vertical Compress", "icons/distribute_vcompress.png");

        private String label;
        private String iconPath;

        DistributeType(String label, String iconPath) {
            this.label = label;
            this.iconPath = iconPath;
        }

        public String getLabel() {
            return label;
        }

        public String getActionID() {
            return "org.csstudio.opibuilder.actions." + toString();
        }

        public ImageDescriptor getImageDescriptor() {
            return CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, iconPath);
        }
    }

    private DistributeType distributeType;

    public DistributeWidgetsAction(IWorkbenchPart part, DistributeType distributeType) {
        super(part);
        this.distributeType = distributeType;
        setText(distributeType.getLabel());
        setId(distributeType.getActionID());
        setImageDescriptor(distributeType.getImageDescriptor());
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedWidgetModels().size() < 2) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        switch (distributeType) {
        case HORIZONTAL_GAP:
            execute(getHorizontalGapCommand());
            break;
        case HORIZONTAL_CENTERS:
            execute(getHorizontalCenterCommand());
            break;
        case HORIZONTAL_COMPRESS:
            execute(getHorizontalCompressCommand());
            break;
        case VERTICAL_GAP:
            execute(getVerticalGapCommand());
            break;
        case VERTICAL_CENTERS:
            execute(getVerticalCenterCommand());
            break;
        case VERTICAL_COMPRESS:
            execute(getVerticalCompressCommand());
            break;
        default:
            break;
        }
    }

    private Command getHorizontalGapCommand() {
        var sortedModelArray = getSortedModelArray(true);
        var cmd = new CompoundCommand("Horizontal Gap Distribution");
        var widthSum = 0;
        for (var i = 1; i < sortedModelArray.length - 1; i++) {
            widthSum += sortedModelArray[i].getWidth();
        }
        var averageGap = (sortedModelArray[sortedModelArray.length - 1].getX()
                - (sortedModelArray[0].getX() + sortedModelArray[0].getWidth()) - widthSum)
                / (sortedModelArray.length - 1);

        var startX = sortedModelArray[0].getX() + sortedModelArray[0].getWidth();
        for (var i = 1; i < sortedModelArray.length - 1; i++) {
            cmd.add(new SetWidgetPropertyCommand(sortedModelArray[i], AbstractWidgetModel.PROP_XPOS,
                    startX + averageGap));
            startX += averageGap + sortedModelArray[i].getWidth();
        }

        return cmd;
    }

    private Command getVerticalGapCommand() {
        var sortedModelArray = getSortedModelArray(false);
        var cmd = new CompoundCommand("Vertical Gap Distribution");
        var widthSum = 0;
        for (var i = 1; i < sortedModelArray.length - 1; i++) {
            widthSum += sortedModelArray[i].getHeight();
        }
        var averageGap = (sortedModelArray[sortedModelArray.length - 1].getY()
                - (sortedModelArray[0].getY() + sortedModelArray[0].getHeight()) - widthSum)
                / (sortedModelArray.length - 1);
        var startX = sortedModelArray[0].getY() + sortedModelArray[0].getHeight();
        for (var i = 1; i < sortedModelArray.length; i++) {
            cmd.add(new SetWidgetPropertyCommand(sortedModelArray[i], AbstractWidgetModel.PROP_YPOS,
                    startX + averageGap));
            startX += averageGap + sortedModelArray[i].getHeight();
        }

        return cmd;
    }

    private Command getHorizontalCenterCommand() {
        var sortedModelArray = getSortedModelArray(true);
        var cmd = new CompoundCommand("Horizontal Center Distribution");

        var averageGap = (getCenterLoc(sortedModelArray[sortedModelArray.length - 1], true)
                - getCenterLoc(sortedModelArray[0], true)) / (sortedModelArray.length - 1);

        var startX = getCenterLoc(sortedModelArray[0], true);
        for (var i = 1; i < sortedModelArray.length - 1; i++) {
            cmd.add(new SetWidgetPropertyCommand(sortedModelArray[i], AbstractWidgetModel.PROP_XPOS,
                    startX + averageGap - sortedModelArray[i].getWidth() / 2));
            startX += averageGap;
        }
        return cmd;
    }

    private Command getVerticalCenterCommand() {
        var sortedModelArray = getSortedModelArray(false);
        var cmd = new CompoundCommand("Vertical Center Distribution");

        var averageGap = (getCenterLoc(sortedModelArray[sortedModelArray.length - 1], false)
                - getCenterLoc(sortedModelArray[0], false)) / (sortedModelArray.length - 1);

        var startX = getCenterLoc(sortedModelArray[0], false);
        for (var i = 1; i < sortedModelArray.length - 1; i++) {
            cmd.add(new SetWidgetPropertyCommand(sortedModelArray[i], AbstractWidgetModel.PROP_YPOS,
                    startX + averageGap - sortedModelArray[i].getHeight() / 2));
            startX += averageGap;
        }
        return cmd;
    }

    private Command getHorizontalCompressCommand() {
        var sortedModelArray = getSortedModelArray(true);
        var cmd = new CompoundCommand("Horizontal Compress Distribution");

        var startX = sortedModelArray[0].getX() + sortedModelArray[0].getWidth();
        for (var i = 1; i < sortedModelArray.length; i++) {
            cmd.add(new SetWidgetPropertyCommand(sortedModelArray[i], AbstractWidgetModel.PROP_XPOS, startX));
            startX += sortedModelArray[i].getWidth();
        }

        return cmd;
    }

    private Command getVerticalCompressCommand() {
        var sortedModelArray = getSortedModelArray(false);
        var cmd = new CompoundCommand("Vertical Compress Distribution");

        var startX = sortedModelArray[0].getY() + sortedModelArray[0].getHeight();
        for (var i = 1; i < sortedModelArray.length; i++) {
            cmd.add(new SetWidgetPropertyCommand(sortedModelArray[i], AbstractWidgetModel.PROP_YPOS, startX));
            startX += sortedModelArray[i].getHeight();
        }

        return cmd;
    }

    private int getCenterLoc(AbstractWidgetModel model, boolean x) {
        if (x) {
            return model.getX() + model.getWidth() / 2;
        } else {
            return model.getY() + model.getHeight() / 2;
        }
    }

    private AbstractWidgetModel[] getSortedModelArray(boolean byHorizontal) {
        var modelArray = new AbstractWidgetModel[getSelectedWidgetModels().size()];
        var i = 0;
        for (var model : getSelectedWidgetModels()) {
            modelArray[i++] = model;
        }
        Arrays.sort(modelArray, (o1, o2) -> {
            int o1loc, o2loc;
            if (byHorizontal) {
                o1loc = o1.getLocation().x;
                o2loc = o2.getLocation().x;
            } else {
                o1loc = o1.getLocation().y;
                o2loc = o2.getLocation().y;
            }
            if (o1loc < o2loc) {
                return -1;
            } else if (o1loc > o2loc) {
                return 1;
            } else {
                return 0;
            }
        });
        return modelArray;
    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final List<AbstractWidgetModel> getSelectedWidgetModels() {
        List<?> selection = getSelectedObjects();

        List<AbstractWidgetModel> selectedWidgetModels = new ArrayList<>();

        for (var o : selection) {
            if (o instanceof AbstractBaseEditPart) {
                selectedWidgetModels.add(((AbstractBaseEditPart) o).getWidgetModel());
            }
        }
        return selectedWidgetModels;
    }
}
