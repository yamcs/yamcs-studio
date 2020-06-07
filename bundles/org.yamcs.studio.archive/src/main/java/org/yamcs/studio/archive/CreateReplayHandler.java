package org.yamcs.studio.archive;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.model.TimeCatalogue;

public class CreateReplayHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
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
                Instant missionTime = TimeCatalogue.getInstance().getMissionTime(true);
                interval = TimeInterval.starting(missionTime.minus(30, ChronoUnit.SECONDS));
            }

            List<String> pps = view.archivePanel.getSelectedPackets("pp");
            Display.getDefault().asyncExec(() -> {
                CreateReplayDialog dialog = new CreateReplayDialog(Display.getCurrent().getActiveShell());
                dialog.initialize(interval, pps);
                dialog.open();
            });
        });

        return null;
    }
}
