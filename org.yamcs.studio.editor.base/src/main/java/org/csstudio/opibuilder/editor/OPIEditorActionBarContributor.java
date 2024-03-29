/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.editor;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.actions.ChangeOrderAction.OrderType;
import org.csstudio.opibuilder.actions.DistributeWidgetsAction.DistributeType;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.AlignmentRetargetAction;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightRetargetAction;
import org.eclipse.gef.ui.actions.MatchWidthRetargetAction;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The action bar contributor for OPI editor.
 */
public class OPIEditorActionBarContributor extends ActionBarContributor {

    public OPIEditorActionBarContributor() {
    }

    @Override
    protected void buildActions() {
        addRetargetAction(new UndoRetargetAction());
        addRetargetAction(new RedoRetargetAction());
        addRetargetAction(new DeleteRetargetAction());

        addRetargetAction(new ZoomInRetargetAction());
        addRetargetAction(new ZoomOutRetargetAction());

        addRetargetAction(new MatchWidthRetargetAction());
        addRetargetAction(new MatchHeightRetargetAction());

        addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.MIDDLE));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.BOTTOM));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.CENTER));
        addRetargetAction(new AlignmentRetargetAction(PositionConstants.RIGHT));

        var a = new RetargetAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY, "Toggle Grid Visibility",
                IAction.AS_CHECK_BOX);
        a.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/grid.png"));
        addRetargetAction(a);

        a = new RetargetAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY, "Toggle Snap To Geometry",
                IAction.AS_CHECK_BOX);
        a.setImageDescriptor(
                AbstractUIPlugin.imageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/snap2geometry.png"));
        addRetargetAction(a);

        a = new RetargetAction(GEFActionConstants.TOGGLE_RULER_VISIBILITY, "Toggle Ruler Visibility",
                IAction.AS_CHECK_BOX);
        a.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/ruler.png"));
        addRetargetAction(a);

        for (var dt : DistributeType.values()) {
            if (dt != DistributeType.HORIZONTAL_GAP) {
                a = new RetargetAction(dt.getActionID(), dt.getLabel());
                a.setImageDescriptor(dt.getImageDescriptor());
                addRetargetAction(a);
            }
        }
        // This is only for action displaying in toolbar
        a = new RetargetAction(DistributeType.HORIZONTAL_GAP.getActionID(), DistributeType.HORIZONTAL_GAP.getLabel(),
                IAction.AS_DROP_DOWN_MENU);
        a.setImageDescriptor(DistributeType.HORIZONTAL_GAP.getImageDescriptor());
        a.setMenuCreator(new IMenuCreator() {
            Menu menu;

            @Override
            public Menu getMenu(Menu parent) {
                return null;
            }

            @Override
            public Menu getMenu(Control parent) {
                if (menu != null) {
                    return menu;
                }
                var manager = new MenuManager();
                for (var dt : DistributeType.values()) {
                    if (dt != DistributeType.HORIZONTAL_GAP) {
                        manager.add(getAction(dt.getActionID()));
                    }
                }
                menu = manager.createContextMenu(parent);
                return menu;
            }

            @Override
            public void dispose() {
                if (menu != null) {
                    menu.dispose();
                    menu = null;
                }
            }
        });
        addRetargetAction(a);
    }

    @Override
    public void contributeToToolBar(IToolBarManager tbm) {
        tbm.add(getAction(ActionFactory.UNDO.getId()));
        tbm.add(getAction(ActionFactory.REDO.getId()));
        tbm.add(getAction(ActionFactory.DELETE.getId()));

        tbm.add(new Separator());
        tbm.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
        tbm.add(getAction(GEFActionConstants.TOGGLE_RULER_VISIBILITY));
        tbm.add(getAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY));

        tbm.add(new Separator());
        tbm.add(getAction(GEFActionConstants.ALIGN_LEFT));
        tbm.add(getAction(GEFActionConstants.ALIGN_CENTER));
        tbm.add(getAction(GEFActionConstants.ALIGN_RIGHT));
        tbm.add(new Separator());
        tbm.add(getAction(GEFActionConstants.ALIGN_TOP));
        tbm.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
        tbm.add(getAction(GEFActionConstants.ALIGN_BOTTOM));

        tbm.add(new Separator());
        tbm.add(getAction(GEFActionConstants.MATCH_WIDTH));
        tbm.add(getAction(GEFActionConstants.MATCH_HEIGHT));

        tbm.add(new Separator());
        tbm.add(getAction(DistributeType.HORIZONTAL_GAP.getActionID()));

        tbm.add(new Separator());
        tbm.add(getAction(GEFActionConstants.ZOOM_IN));
        tbm.add(getAction(GEFActionConstants.ZOOM_OUT));
        tbm.add(new ZoomComboContributionItem(getPage()));
    }

    @Override
    protected void declareGlobalActionKeys() {
        addGlobalActionKey(ActionFactory.PRINT.getId());
        addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
        addGlobalActionKey(ActionFactory.PASTE.getId());
        addGlobalActionKey(ActionFactory.DELETE.getId());
        addGlobalActionKey(ActionFactory.COPY.getId());
        addGlobalActionKey(ActionFactory.CUT.getId());
        addGlobalActionKey(OrderType.TO_FRONT.getActionID());
        addGlobalActionKey(OrderType.TO_BACK.getActionID());
        addGlobalActionKey(OrderType.STEP_BACK.getActionID());
        addGlobalActionKey(OrderType.STEP_FRONT.getActionID());
    }
}
