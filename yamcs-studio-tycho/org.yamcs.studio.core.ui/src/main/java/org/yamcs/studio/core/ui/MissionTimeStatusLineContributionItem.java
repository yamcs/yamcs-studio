package org.yamcs.studio.core.ui;

import org.eclipse.swt.widgets.Display;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.model.TimeListener;
import org.yamcs.studio.core.ui.utils.StatusLineContributionItem;
import org.yamcs.utils.TimeEncoding;

public class MissionTimeStatusLineContributionItem extends StatusLineContributionItem implements TimeListener {

    private static final String DEFAULT_TEXT = "---";

    public MissionTimeStatusLineContributionItem(String id) {
        this(id, 40);
    }

    public MissionTimeStatusLineContributionItem(String id, int charWidth) {
        super(id, charWidth);
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
    public void processTime(long missionTime) {
        Display.getDefault().asyncExec(() -> {
            if (missionTime == TimeEncoding.INVALID_INSTANT) {
                setText(DEFAULT_TEXT);
            } else {
                setText(TimeCatalogue.getInstance().toString(missionTime));
            }
        });
    }
}
