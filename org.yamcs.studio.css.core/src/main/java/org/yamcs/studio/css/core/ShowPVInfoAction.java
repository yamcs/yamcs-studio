/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.css.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Show detailed information of a widget's PVs.
 * <p>
 * If it's a yamcs parameter, the information is enriched, otherwise show the typical CS-Studio content.
 */
public class ShowPVInfoAction implements IObjectActionDelegate {

    private IStructuredSelection selection;
    private IWorkbenchPart targetPart;

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    @Override
    public void run(IAction action) {
        if (getSelectedWidget() == null || getSelectedWidget().getAllPVs() == null
                || getSelectedWidget().getAllPVs().isEmpty()) {
            MessageDialog.openInformation(null, "No PV", "There are no related PVs for this widget");
            return;
        }

        var pvInfos = new ArrayList<PVInfo>();
        getSelectedWidget().getAllPVs().forEach((k, v) -> pvInfos.add(new PVInfo(k, v)));
        Collections.sort(pvInfos);
        loadParameterInfoAndShowDialog(pvInfos);
    }

    /**
     * Gets detailed information on yamcs parameters.
     */
    private void loadParameterInfoAndShowDialog(List<PVInfo> pvInfos) {
        var yamcsPvs = new ArrayList<PVInfo>();
        for (var pvInfo : pvInfos) {
            if (pvInfo.isYamcsParameter()) {
                yamcsPvs.add(pvInfo);
            }
        }

        var futures = new ArrayList<CompletableFuture<Void>>();
        for (var pvInfo : pvInfos) {
            if (!pvInfo.isYamcsParameter()) {
                continue;
            }

            var mdbClient = YamcsPlugin.getMissionDatabaseClient();
            futures.add(mdbClient.getParameter(getYamcsQualifiedName(pvInfo)).handle((response, exc) -> {
                if (exc == null) {
                    pvInfo.setParameterInfo(response);
                } else {
                    pvInfo.setParameterInfoException(exc.getMessage());
                }
                return null;
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((response, exc) -> {
            targetPart.getSite().getShell().getDisplay().asyncExec(() -> showDialog(pvInfos));
        });
    }

    private String getYamcsQualifiedName(PVInfo pvInfo) {
        var displayName = pvInfo.getDisplayName();
        if (displayName.startsWith("para://")) {
            return displayName.substring(7);
        } else if (displayName.startsWith("raw://")) {
            return displayName.substring(6);
        } else if (displayName.startsWith("ops://")) {
            return "MDB:OPS Name/" + displayName.substring(6);
        } else {
            return displayName;
        }
    }

    private void showDialog(List<PVInfo> pvInfos) {
        var dialog = new PVInfoDialog(targetPart.getSite().getShell(), pvInfos);
        dialog.open();
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
