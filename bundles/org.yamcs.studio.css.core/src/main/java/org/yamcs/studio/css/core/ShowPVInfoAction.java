package org.yamcs.studio.css.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.core.model.ParameterCatalogue;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Show detailed information of a widget's PVs.
 * <p>
 * If it's a yamcs parameter, the information is enriched, otherwise show the
 * typical CS-Studio content.
 */
public class ShowPVInfoAction implements IObjectActionDelegate {

    private static final Logger log = Logger.getLogger(ShowPVInfoAction.class.getName());

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

        List<PVInfo> pvInfos = new ArrayList<>();
        getSelectedWidget().getAllPVs().forEach((k, v) -> pvInfos.add(new PVInfo(k, v)));
        Collections.sort(pvInfos);
        loadParameterInfoAndShowDialog(pvInfos);
    }

    /**
     * Gets detailed information on yamcs parameters. We do this one-by-one,
     * because otherwise we risk having one invalid parameter spoil the whole
     * bunch. Idealy we would rewrite this API a bit on yamcs server, so we
     * avoid the use of a latch.
     */
    private void loadParameterInfoAndShowDialog(List<PVInfo> pvInfos) {
        List<PVInfo> yamcsPvs = new ArrayList<>();
        for (PVInfo pvInfo : pvInfos)
            if (pvInfo.isYamcsParameter())
                yamcsPvs.add(pvInfo);

        // Start a worker thread that will show the dialog when a response for
        // all yamcs parameters arrived
        new Thread() {

            @Override
            public void run() {
                CountDownLatch latch = new CountDownLatch(yamcsPvs.size());

                // Another reason why we should have futures
                for (PVInfo pvInfo : pvInfos) {
                    if (!pvInfo.isYamcsParameter()) {
                        latch.countDown();
                        continue;
                    }

                    ParameterCatalogue catalogue = ParameterCatalogue.getInstance();
                    catalogue.requestParameterDetail(pvInfo.getYamcsQualifiedName()).whenComplete((data, exc) -> {
                        if (exc == null) {
                            try {
                                ParameterInfo response = ParameterInfo.parseFrom(data);
                                pvInfo.setParameterInfo(response);
                                latch.countDown();
                            } catch (InvalidProtocolBufferException e) {
                                log.log(Level.SEVERE, "Failed to decode server message", e);
                            }
                        } else {
                            pvInfo.setParameterInfoException(exc.getMessage());
                            latch.countDown();
                        }
                    });
                }

                try {
                    latch.await();
                    targetPart.getSite().getShell().getDisplay().asyncExec(() -> showDialog(pvInfos));
                } catch (InterruptedException e) {
                    targetPart.getSite().getShell().getDisplay().asyncExec(() -> {
                        log.log(Level.SEVERE, "Could not fetch Yamcs parameter info", e);
                        MessageDialog.openError(null, "Could Not Fetch Yamcs Parameter Info",
                                "Interrupted while fetching yamcs parameter info");
                    });
                }
            }
        }.start();
    }

    private void showDialog(List<PVInfo> pvInfos) {
        PVInfoDialog dialog = new PVInfoDialog(targetPart.getSite().getShell(), pvInfos);
        dialog.open();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection)
            this.selection = (IStructuredSelection) selection;
    }

    private AbstractBaseEditPart getSelectedWidget() {
        if (selection.getFirstElement() instanceof AbstractBaseEditPart)
            return (AbstractBaseEditPart) selection.getFirstElement();
        else
            return null;
    }
}
