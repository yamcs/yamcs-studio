/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.util.WidgetsService;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Select widget dialog.
 */
public class WidgetsSelectDialog extends Dialog {

    private TableViewer widgetsViewer;
    private String selectedWidget;
    private int widgetCount;
    private String defaultSelectedWidgetID;
    private boolean onlyPVWidgets;

    /**
     * Constructor
     *
     * @param parentShell
     *            the parent shell.
     * @param widgetCount
     *            Number of widgets that will be created (only for the warning dialog)
     * @param onlyPVWidgets
     *            true if only list PV widgets.
     */
    public WidgetsSelectDialog(Shell parentShell, int widgetCount, boolean onlyPVWidgets) {
        super(parentShell);
        this.widgetCount = widgetCount;
        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
        defaultSelectedWidgetID = "org.csstudio.opibuilder.widgets.TextUpdate";
        this.onlyPVWidgets = onlyPVWidgets;
    }

    public String getOutput() {
        return selectedWidget;
    }

    public void setDefaultSelectedWidgetID(String defaultSelectedWidgetID) {
        this.defaultSelectedWidgetID = defaultSelectedWidgetID;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select the widget to create");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var parent_Composite = (Composite) super.createDialogArea(parent);
        var rightComposite = new Composite(parent_Composite, SWT.NONE);
        rightComposite.setLayout(new GridLayout(1, false));
        var gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 320;
        gd.heightHint = 500;
        rightComposite.setLayoutData(gd);

        widgetsViewer = createWidgetsViewer(rightComposite);
        List<String> widgetsList = new ArrayList<>();
        for (var typeID : WidgetsService.getInstance().getAllWidgetTypeIDs()) {
            if (onlyPVWidgets && WidgetsService.getInstance().getWidgetDescriptor(typeID).getWidgetModel()
                    .getProperty(AbstractPVWidgetModel.PROP_PVNAME) == null) {
                continue;
            }
            widgetsList.add(typeID);
        }

        // sort widgets by name?
        // String[] pvWidgets = pvWidgetList.toArray(new String[0]);
        // Arrays.sort(pvWidgets, new Comparator<String>() {
        //
        // public int compare(String o1, String o2) {
        // String name1 = WidgetsService.getInstance().getWidgetDescriptor(o1).getName();
        // String name2 = WidgetsService.getInstance().getWidgetDescriptor(o2).getName();
        // return name1.compareTo(name2);
        // }
        // });

        widgetsViewer.setInput(widgetsList);
        widgetsViewer.setSelection(new StructuredSelection(defaultSelectedWidgetID));

        if (widgetCount > 1) {
            var bottomComposite = new Composite(parent_Composite, SWT.NONE);
            bottomComposite.setLayout(new GridLayout(2, false));
            gd = new GridData(SWT.FILL, SWT.FILL, true, true);
            bottomComposite.setLayoutData(gd);

            var imageLabel = new Label(bottomComposite, SWT.None);
            if (widgetCount > 30) {
                imageLabel.setImage(Display.getDefault().getSystemImage(SWT.ICON_WARNING));
            }

            var messageLabel = new Label(bottomComposite, SWT.WRAP);
            messageLabel.setText(widgetCount + " widgets will be created."
                    + (widgetCount > 20 ? " It may take a while to create them." : ""));
        }

        return parent_Composite;
    }

    private TableViewer createWidgetsViewer(Composite parent) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new WidgetsListLableProvider());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewer.addSelectionChangedListener(
                event -> selectedWidget = (String) ((StructuredSelection) viewer.getSelection()).getFirstElement());

        viewer.addDoubleClickListener(event -> okPressed());

        return viewer;
    }

    static class WidgetsListLableProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            var typeID = (String) element;
            return WidgetsService.getInstance().getWidgetDescriptor(typeID).getName();
        }

        @Override
        public Image getImage(Object element) {
            var typeID = (String) element;
            var widgetDescriptor = WidgetsService.getInstance().getWidgetDescriptor(typeID);
            var image = CustomMediaFactory.getInstance().getImageFromPlugin(widgetDescriptor.getPluginId(),
                    widgetDescriptor.getIconPath());
            return image;
        }
    }
}
