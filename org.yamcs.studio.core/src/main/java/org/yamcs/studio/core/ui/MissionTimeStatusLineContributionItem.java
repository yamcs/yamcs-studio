package org.yamcs.studio.core.ui;

import java.time.Instant;

import org.eclipse.swt.widgets.Display;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.utils.StatusLineContributionItem;

public class MissionTimeStatusLineContributionItem extends StatusLineContributionItem implements YamcsAware {

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
        YamcsPlugin.addListener(this);
    }

    @Override
    public void updateTime(Instant missionTime) {
        if (isDisposed()) {
            return;
        }
        Display.getDefault().asyncExec(() -> {
            if (missionTime == null) {
                setText(DEFAULT_TEXT);
            } else {
                setText(YamcsPlugin.getDefault().formatInstant(missionTime, true));
            }
        });
    }

    @Override
    public void dispose() {
        YamcsPlugin.removeListener(this);
    }
}
