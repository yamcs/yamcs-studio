/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.editparts;

import org.eclipse.draw2d.ScrollPane;

/**
 * This is an abstract class which represents all Container Editparts which can be scrollable and have a
 * {@link ScrollPane}
 */
public abstract class AbstractScrollableEditpart extends AbstractContainerEditpart {

    /**
     * @return The {@link ScrollPane} of this scrollable EditPart
     */
    public abstract ScrollPane getScrollPane();

}
