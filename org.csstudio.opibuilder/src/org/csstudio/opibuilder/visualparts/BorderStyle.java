/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

/**
 * The style of border.
 */
public enum BorderStyle {

    /**
     * No border.
     */
    NONE("None"),
    /**
     * A line border.
     */
    LINE("Line Style"),
    /**
     * A raised border.
     */
    RAISED("Raised Style"),
    /**
     * A lowered border.
     */
    LOWERED("Lowered Style"),

    /**
     * A etched border.
     */
    ETCHED("Etched Style"),

    /**
     * A ridged border.
     */
    RIDGED("Ridged Style"),

    BUTTON_RAISED("Button Raised Style"),

    BUTTON_PRESSED("Button Pressed Style"),
    /**
     * A dotted border.
     */
    DOTTED("Dot Style"),

    /**
     * A dashed border.
     */
    DASHED("Dash Style"),
    /**
     * A dashed dotted border.
     */
    DASH_DOT("Dash Dot Style"),

    /**
     * A dashed dot dotted border.
     */
    DASH_DOT_DOT("Dash Dot Dot Style"),

    TITLE_BAR("Title Bar Style"),

    GROUP_BOX("Group Box Style"),

    ROUND_RECTANGLE_BACKGROUND("Round Rectangle Background"),

    EMPTY("Empty");

    private String description;

    private BorderStyle(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static String[] stringValues() {
        var sv = new String[values().length];
        var i = 0;
        for (BorderStyle p : values()) {
            sv[i++] = p.toString();
        }
        return sv;
    }

}
