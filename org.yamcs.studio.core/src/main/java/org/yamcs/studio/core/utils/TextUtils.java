/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.utils;

public class TextUtils {

    public static boolean isBlank(String string) {
        return string == null || "".equals(string);
    }

    public static String forceString(Object obj) {
        return (obj != null) ? obj.toString() : "";
    }

    public static String nvl(String str, String replacement) {
        return isBlank(str) ? replacement : str;
    }
}
