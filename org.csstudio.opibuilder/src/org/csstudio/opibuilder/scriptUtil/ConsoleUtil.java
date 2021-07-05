package org.csstudio.opibuilder.scriptUtil;

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
