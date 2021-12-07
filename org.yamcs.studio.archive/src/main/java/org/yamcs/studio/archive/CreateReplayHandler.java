package org.yamcs.studio.archive;

import java.lang.reflect.InvocationTargetException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.CreateProcessorRequest;
import org.yamcs.studio.archive.Histogram.HistogramKind;
import org.yamcs.studio.core.ContextSwitcher;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;

public class CreateReplayHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(CreateReplayHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShellChecked(event);
        var part = HandlerUtil.getActivePartChecked(event);
        SwingUtilities.invokeLater(() -> {
            var view = (ArchiveView) part;

            TimeInterval interval;
            var selectionStart = view.getTimeline().getSelectionStart();
            var selectionStop = view.getTimeline().getSelectionStop();
            if (selectionStart != null) {
                var start = selectionStart.toInstant();
                var stop = selectionStop.toInstant();
                interval = new TimeInterval(start, stop);
            } else {
                var missionTime = YamcsPlugin.getMissionTime(true);
                interval = TimeInterval.starting(missionTime.minus(30, ChronoUnit.SECONDS));
            }

            var pps = new ArrayList<String>();
            view.getTimeline().getHistograms(HistogramKind.PP).forEach(histogram -> {
                pps.add(histogram.getLabel());
            });
            Collections.sort(pps);

            Display.getDefault().asyncExec(() -> {
                var dialog = new CreateReplayDialog(Display.getCurrent().getActiveShell());
                dialog.initialize(interval, pps);
                var result = dialog.open();
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
            var cause = e.getCause();
            log.log(Level.SEVERE, "Failed to switch processor", cause);
            MessageDialog.openError(shell, "Failed to switch processor", cause.getMessage());
        } catch (InterruptedException e) {
            log.info("Processor switch cancelled");
        }
    }
}
