/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
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
