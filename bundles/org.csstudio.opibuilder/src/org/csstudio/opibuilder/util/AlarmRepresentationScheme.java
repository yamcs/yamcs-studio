/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.util;

import org.csstudio.opibuilder.visualparts.BorderFactory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.diirt.vtype.AlarmSeverity;
import org.eclipse.draw2d.Border;
import org.eclipse.swt.graphics.RGB;

/**
 * The scheme for alarm color which provide unified colors and borders for alarms.
 * 
 * @author Xihui Chen
 * @author Takashi Nakamoto - implemented getAlarmColor() method.
 */
public class AlarmRepresentationScheme {

    public static final int ALARM_BORDER_WIDTH = 2;
    public static final String MAJOR = "Major";
    public static final String MINOR = "Minor";
    public static final String INVALID = "Invalid";
    public static final String DISCONNECTED = "Disconnected";

    /**
     * Returns color of alarm severity.
     * 
     * @param severity
     * @return RGB color of the given alarm severity. Null if alarm severity is "OK".
     */
    public static RGB getAlarmColor(AlarmSeverity severity) {
        switch (severity) {
        case MAJOR:
            return getMajorColor();
        case NONE:
            return null;
        case MINOR:
            return getMinorColor();
        case INVALID:
        case UNDEFINED:
        default:
            return getInvalidColor();
        }
    }

    public static RGB getMajorColor() {
        return MediaService.getInstance().getColor(MAJOR);
    }

    public static RGB getMinorColor() {
        return MediaService.getInstance().getColor(MINOR);
    }

    public static RGB getInvalidColor() {
        return MediaService.getInstance().getColor(INVALID);
    }

    public static RGB getDisconnectedColor() {
        return MediaService.getInstance().getColor(DISCONNECTED);
    }

    public static Border getMajorBorder(BorderStyle borderStyle) {
        BorderStyle newBorderStyle = getNewBorderStyle(borderStyle);
        return BorderFactory.createBorder(newBorderStyle, ALARM_BORDER_WIDTH, getMajorColor(), "");
    }

    private static BorderStyle getNewBorderStyle(BorderStyle borderStyle) {
        BorderStyle newBorderStyle = BorderStyle.LINE;
        switch (borderStyle) {
        case DASH_DOT:
        case DASH_DOT_DOT:
        case DASHED:
        case DOTTED:
            newBorderStyle = borderStyle;
            break;
        default:
            break;
        }
        return newBorderStyle;
    }

    public static Border getMinorBorder(BorderStyle borderStyle) {
        BorderStyle newBorderStyle = getNewBorderStyle(borderStyle);
        return BorderFactory.createBorder(newBorderStyle, ALARM_BORDER_WIDTH, getMinorColor(), "");
    }

    public static Border getInvalidBorder(BorderStyle borderStyle) {
        BorderStyle newBorderStyle = getNewBorderStyle(borderStyle);
        return BorderFactory.createBorder(newBorderStyle, ALARM_BORDER_WIDTH, getInvalidColor(), "");
    }

    public static Border getDisconnectedBorder() {
        return BorderFactory.createBorder(BorderStyle.TITLE_BAR, 1, getDisconnectedColor(),
                "Disconnected");
    }
}
