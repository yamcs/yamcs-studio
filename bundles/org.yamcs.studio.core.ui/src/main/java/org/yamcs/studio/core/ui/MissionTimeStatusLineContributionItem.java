package org.yamcs.studio.core.ui;

import java.time.Instant;

import org.eclipse.swt.widgets.Display;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.model.TimeListener;
import org.yamcs.studio.core.ui.utils.StatusLineContributionItem;

public class MissionTimeStatusLineContributionItem extends StatusLineContributionItem implements TimeListener {

    private static final String DEFAULT_TEXT = "---";

    public MissionTimeStatusLineContributionItem(String id) {
        this(id, CALC_TRUE_WIDTH, false);
    }

    public MissionTimeStatusLineContributionItem(String id, boolean addTrailingSeparator) {
        this(id, CALC_TRUE_WIDTH, addTrailingSeparator);
    }

    public MissionTimeStatusLineContributionItem(String id, int charWidth, boolean addTrailingSeparator) {
        super(id, charWidth, addTrailingSeparator);
        setText(DEFAULT_TEXT);
        setToolTipText("Mission Time");
        TimeCatalogue.getInstance().addTimeListener(this);
    }

    @Override
    public void dispose() {
        TimeCatalogue catalogue = TimeCatalogue.getInstance();
        if (catalogue != null) {
            catalogue.removeTimeListener(this);
        }
    }

    @Override
    public void processTime(Instant missionTime) {
        if (isDisposed()) {
            return;
        }
        Display.getDefault().asyncExec(() -> {
            if (missionTime == null) {
                setText(DEFAULT_TEXT);
            } else {
                setText(YamcsUIPlugin.getDefault().formatInstant(missionTime, true));
            }
        });
    }
}
