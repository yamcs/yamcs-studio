package org.yamcs.studio.data.vtype;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for frequently used NumberFormats.
 *
 * @author carcassi
 */
public final class NumberFormats {

    private static final Map<Integer, DecimalFormat> precisionFormat = new ConcurrentHashMap<>();
    private static volatile Locale currentLocale;
    private static volatile DecimalFormatSymbols symbols;

    static {
        Locale newLocale = Locale.getDefault();
        DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(newLocale);
        newSymbols.setNaN("NaN");
        newSymbols.setInfinity("Infinity");
        currentLocale = newLocale;
        symbols = newSymbols;
    }

    private NumberFormats() {
        // Prevent instances
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
        NumberFormat format = precisionFormat.get(precision);
        if (format == null) {
            precisionFormat.put(precision, createFormatter(precision));
        }
        return precisionFormat.get(precision);
    }

    @SuppressWarnings("serial")
    private static NumberFormat toStringFormat = new NumberFormat() {

        // This override was added as per issue #109.
        // Java's NumberFormat floats using Float.doubleValue() instead of Float.floatValue(),
        // which can lead to undesired rounding issues.
        // For example we want 838.12f to print as 838.12, not 838.11999951171875
        @Override
        public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
            if (number instanceof Float) {
                toAppendTo.append(number);
                return toAppendTo;
            } else {
                return super.format(number, toAppendTo, pos);
            }
        }

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(number);
            return toAppendTo;
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(number);
            return toAppendTo;
        }

        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    /**
     * Returns the NumberFormat that uses the standard text representation (toString) for numbers. Parsing is not
     * currently supported.
     *
     * @return a number format
     */
    public static NumberFormat toStringFormat() {
        return toStringFormat;
    }

    /**
     * Creates a new number format that formats a number with the given number of precision digits.
     *
     * @param precision
     *            number of digits past the decimal point
     * @return a number format
     */
    private static DecimalFormat createFormatter(int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("Precision must be non-negative");
        }

        if (precision == 0) {
            return new DecimalFormat("0", symbols);
        }

        StringBuilder sb = new StringBuilder("0.");
        for (int i = 0; i < precision; i++) {
            sb.append("0");
        }
        return new DecimalFormat(sb.toString(), symbols);
    }

}
