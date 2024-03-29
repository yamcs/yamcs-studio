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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.commands.ChangeOrderCommand;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The action which changes the order of widget.
 */
public class ChangeOrderAction extends SelectionAction {

    public enum OrderType {
        TO_FRONT("Bring to Front", "icons/shape_move_front.png"),
        TO_BACK("Send to Back", "icons/shape_move_back.png"),
        STEP_FRONT("Bring Forward", "icons/shape_move_forwards.png"),
        STEP_BACK("Send Backward", "icons/shape_move_backwards.png");

        private String label;
        private String iconPath;

        OrderType(String label, String iconPath) {
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

    private static class IndexedWidget implements Comparable<IndexedWidget> {

        private Integer index;

        private AbstractWidgetModel widget;

        public IndexedWidget(int index, AbstractWidgetModel widget) {
            this.index = index;
            this.widget = widget;
        }

        /**
         * @return the index
         */
        public final int getIndex() {
            return index;
        }

        /**
         * @return the widget
         */
        public final AbstractWidgetModel getWidget() {
            return widget;
        }

        @Override
        public int compareTo(IndexedWidget o) {
            return index.compareTo(Integer.valueOf(o.getIndex()));
        }

        @Override
        public int hashCode() {
            return Objects.hash(index);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            var other = (IndexedWidget) obj;
            if (!Objects.equals(index, other.index)) {
                return false;
            }
            return true;
        }
    }

    private OrderType orderType;

    public ChangeOrderAction(IWorkbenchPart part, OrderType orderType) {
        super(part);
        this.orderType = orderType;
        setId(orderType.getActionID());
        setActionDefinitionId(orderType.getActionID());
        setText(orderType.getLabel());
        setImageDescriptor(orderType.getImageDescriptor());
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().size() == 0
                || getSelectedObjects().size() == 1 && getSelectedObjects().get(0) instanceof EditPart
                        && ((EditPart) getSelectedObjects().get(0)).getModel() instanceof DisplayModel) {
            return false;
        }
        Map<AbstractContainerModel, List<IndexedWidget>> widgetMap = new HashMap<>();
        fillWidgetMap(widgetMap);

        // create compound command
        for (var entry : widgetMap.entrySet()) {
            // sort the list in map by the widget's original order in its container
            var container = entry.getKey();
            var widgetList = entry.getValue();
            Collections.sort(widgetList);

            int newIndex;
            switch (orderType) {
            case TO_FRONT:
                newIndex = container.getChildren().size() - 1;
                break;
            case STEP_FRONT:
                newIndex = widgetList.get(widgetList.size() - 1).getIndex() + 1;
                break;
            case STEP_BACK:
                newIndex = widgetList.get(0).getIndex() - 1;
                break;
            case TO_BACK:
            default:
                newIndex = 0;
                break;
            }
            if (newIndex > container.getChildren().size() - 1 || newIndex < 0) {
                return false;
            }
            for (var indexedWidget : widgetList) {
                if (container.getIndexOf(indexedWidget.getWidget()) != newIndex) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void run() {
        Map<AbstractContainerModel, List<IndexedWidget>> widgetMap = new HashMap<>();
        fillWidgetMap(widgetMap);

        var compoundCommand = new CompoundCommand(orderType.getLabel());

        // create compound command
        for (var entry : widgetMap.entrySet()) {
            // sort the list in map by the widget's original order in its container
            var container = entry.getKey();
            var widgetList = entry.getValue();
            Collections.sort(widgetList);

            int newIndex;
            switch (orderType) {
            case TO_FRONT:
                newIndex = container.getChildren().size() - 1;
                break;
            case STEP_FRONT:
                newIndex = widgetList.get(widgetList.size() - 1).getIndex() + 1;
                break;
            case STEP_BACK:
                newIndex = widgetList.get(0).getIndex() - 1;
                break;
            case TO_BACK:
            default:
                newIndex = 0;
                break;
            }

            // reorder
            switch (orderType) {
            case TO_FRONT:
            case STEP_FRONT:
                for (var indexedWidget : widgetList) {
                    compoundCommand.add(new ChangeOrderCommand(newIndex, container, indexedWidget.getWidget()));
                }
                break;
            case STEP_BACK:
            case TO_BACK:
                for (var i = widgetList.size() - 1; i >= 0; i--) {
                    compoundCommand.add(new ChangeOrderCommand(newIndex, container, widgetList.get(i).getWidget()));
                }
                break;
            default:
                break;
            }
        }
        execute(compoundCommand);
    }

    /**
     * @param widgetMap
     */
    private void fillWidgetMap(Map<AbstractContainerModel, List<IndexedWidget>> widgetMap) {

        for (var selection : getSelectedObjects()) {
            if (selection instanceof AbstractBaseEditPart) {
                var widgetEditpart = (AbstractBaseEditPart) selection;
                var widgetModel = (AbstractWidgetModel) widgetEditpart.getModel();
                if (widgetEditpart.getParent() != null && widgetModel.getParent() != null) {
                    var containerModel = (AbstractContainerModel) widgetEditpart.getParent().getModel();

                    if (!widgetMap.containsKey(containerModel)) {
                        widgetMap.put(containerModel, new LinkedList<IndexedWidget>());
                    }
                    widgetMap.get(containerModel)
                            .add(new IndexedWidget(containerModel.getIndexOf(widgetModel), widgetModel));
                }
            }
        }
    }
}
