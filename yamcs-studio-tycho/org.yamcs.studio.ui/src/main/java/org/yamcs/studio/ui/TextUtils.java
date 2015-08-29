package org.yamcs.studio.ui;

public class TextUtils {

    public static boolean isBlank(String string) {
        return string == null || "".equals(string);
    }

    public static String forceString(Object obj) {
        return (obj != null) ? obj.toString() : "";
    }
}
