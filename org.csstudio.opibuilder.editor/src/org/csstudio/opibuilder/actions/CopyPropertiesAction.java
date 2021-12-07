/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
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
import org.csstudio.opibuilder.editor.OPIEditor;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.visualparts.PropertiesSelectDialog;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.Transfer;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * The action that only copy properties from a widget.
 */
public class CopyPropertiesAction extends SelectionAction {

    private static final String ROOT_ELEMENT = "PropCopyData";

    public static final String PROPID_ELEMENT = "Properties";

    public static final String ID = "org.csstudio.opibuilder.actions.copyproperties";

    /**
     * @param part
     *            the OPI Editor
     * @param pasteWidgetsAction
     *            pass the paste action will help to update the enable state of the paste action after copy action
     *            invoked.
     */
    public CopyPropertiesAction(OPIEditor part) {
        super(part);
        setText("Copy Properties...");
        setId(ID);
        setImageDescriptor(CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID,
                "icons/copy_properties.png"));
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedWidgetModels().size() == 1 && !(getSelectedWidgetModels().get(0) instanceof DisplayModel)) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        var dialog = new PropertiesSelectDialog(null, getSelectedWidgetModels().get(0));
        if (dialog.open() == Window.OK) {
            var propList = dialog.getOutput();
            if (!propList.isEmpty()) {
                var widget = getSelectedWidgetModels().get(0);
                var widgetElement = XMLUtil.widgetToXMLElement(widget);

                var propertisElement = new Element(PROPID_ELEMENT);

                for (String propID : propList) {
                    propertisElement.addContent(new Element(propID));
                }
                var rootElement = new Element(ROOT_ELEMENT);

                rootElement.addContent(widgetElement);
                rootElement.addContent(propertisElement);

                var xmlOutputter = new XMLOutputter(Format.getRawFormat());
                var xmlString = xmlOutputter.outputString(rootElement);

                ((OPIEditor) getWorkbenchPart()).getClipboard().setContents(new Object[] { xmlString },
                        new Transfer[] { PropertiesCopyDataTransfer.getInstance() });
            }
        }

    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final List<AbstractWidgetModel> getSelectedWidgetModels() {
        List<?> selection = getSelectedObjects();

        List<AbstractWidgetModel> selectedWidgetModels = new ArrayList<AbstractWidgetModel>();

        for (Object o : selection) {
            if (o instanceof EditPart) {
                selectedWidgetModels.add((AbstractWidgetModel) ((EditPart) o).getModel());
            }
        }
        return selectedWidgetModels;
    }

}
