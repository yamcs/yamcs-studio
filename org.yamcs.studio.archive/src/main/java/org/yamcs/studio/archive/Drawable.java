/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.archive;

import org.eclipse.swt.graphics.GC;

public abstract class Drawable {

    protected Timeline timeline;

    public Drawable(Timeline timeline) {
        this.timeline = timeline;
        timeline.add(this);
    }

    protected void reportMutation() {
        timeline.requestRepaint();
    }

    void beforeDraw(GC gc) {
    }

    void drawUnderlay(GC gc) {
    }

    void drawContent(GC gc) {
    }

    void drawOverlay(GC gc) {
    }
}
