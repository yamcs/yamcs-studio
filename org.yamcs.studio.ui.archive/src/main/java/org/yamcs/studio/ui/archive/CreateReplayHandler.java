package org.yamcs.studio.ui.archive;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.model.TimeCatalogue;

public class CreateReplayHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        long missionTime = TimeCatalogue.getInstance().getMissionTime(true);
        missionTime -= 30 * 1000;
        TimeInterval interval = TimeInterval.starting(missionTime);
        Display.getDefault().asyncExec(() -> {
            CreateReplayDialog dialog = new CreateReplayDialog(Display.getCurrent().getActiveShell());
            dialog.initialize(interval);
            dialog.open();
        });

        return null;
    }
}
