package org.yamcs.studio.archive;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.CreateProcessorRequest;
import org.yamcs.studio.core.ContextSwitcher;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;

public class CreateReplayHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(CreateReplayHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShellChecked(event);
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        SwingUtilities.invokeLater(() -> {
            ArchiveView view = (ArchiveView) part;

            TimeInterval interval;
            Selection sel = view.archivePanel.getSelection();
            if (sel != null) {
                Instant start = Instant.ofEpochMilli(sel.getStartInstant());
                Instant stop = Instant.ofEpochMilli(sel.getStopInstant());
                interval = new TimeInterval(start, stop);
            } else {
                Instant missionTime = YamcsPlugin.getMissionTime(true);
                interval = TimeInterval.starting(missionTime.minus(30, ChronoUnit.SECONDS));
            }

            List<String> pps = view.archivePanel.getSelectedPackets("pp");
            Display.getDefault().asyncExec(() -> {
                CreateReplayDialog dialog = new CreateReplayDialog(Display.getCurrent().getActiveShell());
                dialog.initialize(interval, pps);
                int result = dialog.open();
                if (result == Dialog.OK) {
                    switchToReplay(shell, dialog.getRequest());
                }
            });
        });

        return null;
    }

    private void switchToReplay(Shell shell, CreateProcessorRequest request) {
        try {
            new ProgressMonitorDialog(shell).run(true, true,
                    new ContextSwitcher(request.getInstance(), request.getName()));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            log.log(Level.SEVERE, "Failed to switch processor", cause);
            MessageDialog.openError(shell, "Failed to switch processor", cause.getMessage());
        } catch (InterruptedException e) {
            log.info("Processor switch cancelled");
        }
    }
}
