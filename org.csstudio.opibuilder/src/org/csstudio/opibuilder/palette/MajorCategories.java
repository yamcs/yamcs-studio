/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.palette;

/**
 * The Major categories of widgets on the palette.
 */
public enum MajorCategories {

    GRAPHICS("Graphics"),

    MONITORS("Monitors"),

    CONTROLS("Controls"),

    OTHERS("Others");

    private String description;

    private MajorCategories(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
