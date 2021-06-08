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
