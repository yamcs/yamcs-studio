/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.script;

import java.util.logging.Logger;

public class ConsoleUtil {

    private static final Logger log = Logger.getLogger("org.yamcs.studio.script");

    /**
     * Write information to console.
     */
    public static void writeInfo(String message) {
        log.info(message);
    }

    /**
     * Write Error information to console.
     */
    public static void writeError(String message) {
        log.severe(message);
    }

    /**
     * Write Warning information to console.
     */
    public static void writeWarning(String message) {
        log.warning(message);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void writeString(String string) {
        writeInfo(string);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void writeString(String string, int red, int green, int blue) {
        writeInfo(string);
    }
}
