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
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.datadefinition.PropertiesCopyData;
import org.csstudio.opibuilder.editor.OPIEditor;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The action that paste the properties from clipboard.
 */
public class PastePropertiesAction extends SelectionAction {

    public static final String ID = "org.csstudio.opibuilder.actions.pasteproperties";

    public PastePropertiesAction(IWorkbenchPart part) {
        super(part);
        setText("Paste Properties");
        setId(ID);
        setImageDescriptor(CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID,
                "icons/paste_properties.png"));
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedWidgetModels().size() > 0 && getPropetiesCopyDataFromClipboard() != null) {
            return true;
        }
        return false;
    }

    public Command createPasteCommand() {
        var propData = getPropetiesCopyDataFromClipboard();
        var cmd = new CompoundCommand("Paste Properties");

        for (var targetWidget : getSelectedWidgetModels()) {
            for (var prop_id : propData.getPropIDList()) {
                if (targetWidget.getAllPropertyIDs().contains(prop_id)) {
                    cmd.add(new SetWidgetPropertyCommand(targetWidget, prop_id,
                            propData.getWidgetModel().getPropertyValue(prop_id)));
                }
            }
        }

        return cmd;
    }

    @Override
    public void run() {
        execute(createPasteCommand());
    }

    /**
     * Returns a list with widget models that are currently stored on the clipboard.
     *
     * @return a list with widget models or an empty list
     */
    private PropertiesCopyData getPropetiesCopyDataFromClipboard() {
        var result = getOPIEditor().getClipboard().getContents(PropertiesCopyDataTransfer.getInstance());
        if (result != null && result instanceof PropertiesCopyData) {
            return (PropertiesCopyData) result;
        }
        return null;
    }

    /**
     * Returns the currently open OPI editor.
     *
     * @return the currently open OPI editor
     */
    private OPIEditor getOPIEditor() {

        return (OPIEditor) getWorkbenchPart();
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
            if (o instanceof EditPart) {
                selectedWidgetModels.add((AbstractWidgetModel) ((EditPart) o).getModel());
            }
        }
        return selectedWidgetModels;
    }
}
