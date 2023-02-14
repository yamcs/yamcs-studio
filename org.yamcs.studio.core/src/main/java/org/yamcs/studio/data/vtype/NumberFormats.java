/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for frequently used NumberFormats.
 */
public final class NumberFormats {

    private static final Map<Integer, DecimalFormat> FORMAT_BY_PRECISION = new ConcurrentHashMap<>();

    /**
     * Localized {@link DecimalFormat} symbols.
     */
    public static final DecimalFormatSymbols DF_SYMBOLS;

    /**
     * Uses the standard, but localized, text representation (toString) for numbers
     */
    public static final NumberFormat TO_STRING_FORMAT = DecimalFormat.getInstance();

    static {
        var locale = Locale.getDefault();
        DF_SYMBOLS = new DecimalFormatSymbols(locale);
        DF_SYMBOLS.setNaN("NaN");
        DF_SYMBOLS.setInfinity("Infinity");

        ((DecimalFormat) TO_STRING_FORMAT).setDecimalFormatSymbols(DF_SYMBOLS);
        ((DecimalFormat) TO_STRING_FORMAT).setGroupingUsed(false);
    }

    /**
     * Returns a number format that formats a number with the given number of precision digits. Parsing is not currently
     * supported.
     *
     * @param precision
     *            number of digits past the decimal point
     * @return a number format
     */
    public static NumberFormat format(int precision) {
        return FORMAT_BY_PRECISION.computeIfAbsent(precision, k -> {
            if (precision < 0) {
                throw new IllegalArgumentException("Precision must be non-negative");
            } else if (precision == 0) {
                return new DecimalFormat("0", DF_SYMBOLS);
            } else {
                var sb = new StringBuilder("0.");
                for (var i = 0; i < precision; i++) {
                    sb.append("0");
                }
                return new DecimalFormat(sb.toString(), DF_SYMBOLS);
            }
        });
    }
}
