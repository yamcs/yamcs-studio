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

import java.time.Instant;
import java.util.Comparator;

import com.google.protobuf.Timestamp;

public class Comparators {

    // Some null-safe comparators
    public static final Comparator<String> STRING_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null) {
            return (o1 == null) ? -1 : 1;
        }
        if (o1 == null && o2 == null) {
            return 0;
        }
        return o1.compareToIgnoreCase(o2);
    };

    public static final Comparator<Integer> INTEGER_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null) {
            return (o1 == null) ? -1 : 1;
        }
        if (o1 == null && o2 == null) {
            return 0;
        }
        return o1.compareTo(o2);
    };

    public static final Comparator<Long> LONG_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null) {
            return (o1 == null) ? -1 : 1;
        }
        if (o1 == null && o2 == null) {
            return 0;
        }
        return o1.compareTo(o2);
    };

    public static final Comparator<Timestamp> TIMESTAMP_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null) {
            return (o1 == null) ? -1 : 1;
        }
        if (o1 == null && o2 == null) {
            return 0;
        }
        var rc = Long.compare(o1.getSeconds(), o2.getSeconds());
        return rc != 0 ? rc : Integer.compare(o1.getNanos(), o2.getNanos());
    };

    public static final Comparator<Instant> INSTANT_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null) {
            return (o1 == null) ? -1 : 1;
        }
        if (o1 == null && o2 == null) {
            return 0;
        }
        var rc = Long.compare(o1.getEpochSecond(), o2.getEpochSecond());
        return rc != 0 ? rc : Integer.compare(o1.getNano(), o2.getNano());
    };

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final Comparator<Comparable<?>> OBJECT_COMPARATOR = (o1, o2) -> {
        if (o1 == null ^ o2 == null) {
            return (o1 == null) ? -1 : 1;
        }
        if (o1 == null && o2 == null) {
            return 0;
        }
        return ((Comparable) o1).compareTo(o2);
    };
}
