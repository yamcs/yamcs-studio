package org.yamcs.studio.core.ui;

import java.util.Comparator;

public class Comparators {

    // Some null-safe comparators
    public static final Comparator<String> STRING_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null)
            return (o1 == null) ? -1 : 1;
        if (o1 == null && o2 == null)
            return 0;
        return o1.compareTo(o2);
    };

    public static final Comparator<Integer> INTEGER_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null)
            return (o1 == null) ? -1 : 1;
        if (o1 == null && o2 == null)
            return 0;
        return o1.compareTo(o2);
    };

    public static final Comparator<Long> LONG_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null)
            return (o1 == null) ? -1 : 1;
        if (o1 == null && o2 == null)
            return 0;
        return o1.compareTo(o2);
    };
}
