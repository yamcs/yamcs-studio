/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util;

import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.ZoomManager;

public class Draw2dSingletonUtil {

    public static String ZoomManager_FIT_ALL = ZoomManager.FIT_ALL;
    public static String ZoomManager_FIT_WIDTH = ZoomManager.FIT_WIDTH;
    public static String ZoomManager_FIT_HEIGHT = ZoomManager.FIT_HEIGHT;

    public static TextUtilities getTextUtilities() {
        return TextUtilities.INSTANCE;
    }

    public static Rectangle getRectangle() {
        return Rectangle.SINGLETON;
    }
}
