/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.actions;

import java.util.Map;
import java.util.Map.Entry;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.VTypeHelper;

/**
 * Show details information of widget's primary PV.
 */
public class ShowPVInfoAction implements IObjectActionDelegate {

    private final class PVsInfoDialog extends MessageDialog {

        private Map<String, IPV> pvMap;

        public PVsInfoDialog(Shell parentShell, String dialogTitle, Map<String, IPV> pvMap) {
            super(parentShell, dialogTitle, null, "PVs' details on this widget:", MessageDialog.INFORMATION,
                    new String[] { JFaceResources.getString("ok") }, 0);
            this.pvMap = pvMap;
        }

        @Override
        protected Control createCustomArea(Composite parent) {
            if (pvMap == null || pvMap.size() == 0) {
                return super.createCustomArea(parent);
            }
            parent.setLayout(new FillLayout());
            var tabFolder = new TabFolder(parent, SWT.None);
            for (Entry<String, IPV> entry : pvMap.entrySet()) {
                var tabItem = new TabItem(tabFolder, SWT.None);
                tabItem.setText(entry.getKey());
                var text = new Text(tabFolder, SWT.MULTI | SWT.READ_ONLY);
                text.setText(getPVInfo(entry.getValue()));
                tabItem.setControl(text);

            }
            return tabFolder;

        }

    }

    private IStructuredSelection selection;
    private IWorkbenchPart targetPart;

    public ShowPVInfoAction() {
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    @Override
    public void run(IAction action) {
        if (getSelectedWidget() == null || getSelectedWidget().getAllPVs() == null
                || getSelectedWidget().getAllPVs().size() == 0) {
            MessageDialog.openInformation(null, "No PV", "No related PV on this widget.");
            return;
        }

        var dialog = new PVsInfoDialog(targetPart.getSite().getShell(), "PV Info", getSelectedWidget().getAllPVs());
        dialog.open();

    }

    private String getPVInfo(IPV pv) {
        var stateInfo = new StringBuilder();
        if (!pv.isStarted()) {
            stateInfo.append("Not started");
        } else if (pv.isConnected()) {
            stateInfo.append("Connected");
        } else {
            stateInfo.append("Connecting");
        }

        var sb = new StringBuilder();
        sb.append("Name: " + pv.getName() + "\n");
        sb.append("State: " + stateInfo + "\n");
        if (pv.getValue() != null) {
            sb.append((pv.isConnected() ? "Value: " : "Last received value: ") + pv.getValue() + "\n");
            sb.append("Display Info: ");
            var displayInfo = VTypeHelper.getDisplayInfo(pv.getValue());
            if (displayInfo != null) {
                sb.append("\nUnits: ");
                sb.append(displayInfo.getUnits());
                sb.append("\nPrecision: ");
                sb.append(displayInfo.getFormat().getMaximumFractionDigits());
                sb.append("\nControl_Low: ");
                sb.append(displayInfo.getLowerCtrlLimit());
                sb.append("\nControl_High :");
                sb.append(displayInfo.getUpperCtrlLimit());
                sb.append("\nDisplay_Low: ");
                sb.append(displayInfo.getLowerDisplayLimit());
                sb.append("\nDisplay_High :");
                sb.append(displayInfo.getUpperDisplayLimit());
                sb.append("\nAlarm_Low: ");
                sb.append(displayInfo.getLowerAlarmLimit());
                sb.append("\nWarning_Low: ");
                sb.append(displayInfo.getLowerWarningLimit());
                sb.append("\nWarning_High: ");
                sb.append(displayInfo.getUpperWarningLimit());
                sb.append("\nAlarm_High: ");
                sb.append(displayInfo.getUpperAlarmLimit());
            } else {
                sb.append("null");
            }
        } else {
            sb.append("Value: null");
        }
        return sb.toString();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            this.selection = (IStructuredSelection) selection;
        }
    }

    private AbstractBaseEditPart getSelectedWidget() {
        if (selection.getFirstElement() instanceof AbstractBaseEditPart) {
            return (AbstractBaseEditPart) selection.getFirstElement();
        } else {
            return null;
        }
    }

}
