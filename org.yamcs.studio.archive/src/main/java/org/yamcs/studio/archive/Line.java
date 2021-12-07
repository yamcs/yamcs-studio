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

import org.eclipse.swt.graphics.Rectangle;

public abstract class Line extends Drawable {

    Rectangle coords;
    private boolean frozen;

    public Line(Timeline timeline) {
        super(timeline);
    }

    public abstract int getHeight();

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isFrozen() {
        return frozen;
    }
}
