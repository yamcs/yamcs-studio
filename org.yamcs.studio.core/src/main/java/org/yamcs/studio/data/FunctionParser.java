/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.yamcs.studio.data.vtype.ArrayDouble;
import org.yamcs.studio.data.vtype.ListDouble;

/**
 * Utility class to parse variable names and create simulated signals.
 */
public class FunctionParser {

    /**
     * The pattern of a string fragment with escape sequences.
     */
    public static final String STRING_ESCAPE_SEQUENCE_REGEX = "\\\\(\"|\\\\|\'|r|n|b|t|u[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]|[0-3]?[0-7]?[0-7])";

    /**
     * The pattern of a string, including double quotes.
     */
    public static final String QUOTED_STRING_REGEX = "\"([^\"\\\\]|" + STRING_ESCAPE_SEQUENCE_REGEX + ")*\"";

    /**
     * The pattern of a double value.
     */
    public static final String DOUBLE_REGEX = "([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";

    private static Pattern escapeSequence = Pattern.compile(STRING_ESCAPE_SEQUENCE_REGEX);

    /**
     * Parse a function that accepts a scalar value (number or string) or an array value (number or string).
     *
     * @param string
     *            the string to be parsed
     * @param errorMessage
     *            the error message
     * @return the name of the function and the argument
     */
    public static List<Object> parseFunctionWithScalarOrArrayArguments(String string, String errorMessage) {
        return parseFunctionWithScalarOrArrayArguments("(\\w+)", string, errorMessage);
    }

    /**
     * Parse a function that accepts a scalar value (number or string) or an array value (number or string).
     *
     * @param nameRegex
     *            regex for function name
     * @param string
     *            the string to be parsed
     * @param errorMessage
     *            the error message for the exception if parsing fails
     * @return the name of the function and the argument
     */
    public static List<Object> parseFunctionWithScalarOrArrayArguments(String nameRegex, String string,
            String errorMessage) {
        // Parse the channel name
        var parsedTokens = FunctionParser.parseFunctionAnyParameter(nameRegex, string);

        // Parsing failed
        if (parsedTokens == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        // Single argument, return right away
        if (parsedTokens.size() <= 2) {
            return parsedTokens;
        }

        // Multiple arguments, collect in array if possible
        var data = asScalarOrList(parsedTokens.subList(1, parsedTokens.size()));
        if (data == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return Arrays.asList(parsedTokens.get(0), data);
    }

    /**
     * Converts the list of arguments into a scalar or an appropriate list. Returns null if it's not possible.
     *
     * @param objects
     *            the argument list
     * @return the value converted or null
     */
    public static Object asScalarOrList(List<Object> objects) {
        if (objects.isEmpty()) {
            return null;
        } else if (objects.size() == 1) {
            return objects.get(0);
        } else if (objects.get(0) instanceof Double) {
            return asListDouble(objects);
        } else if (objects.get(0) instanceof String) {
            return asListString(objects);
        } else {
            return null;
        }
    }

    /**
     * Convert the list of arguments to a ListDouble. Returns null if it's not possible.
     *
     * @param objects
     *            a list of arguments
     * @return the converted list or null
     */
    public static ListDouble asListDouble(List<Object> objects) {
        var data = new double[objects.size()];
        for (var i = 0; i < objects.size(); i++) {
            var value = objects.get(i);
            if (value instanceof Double) {
                data[i] = (Double) value;
            } else {
                return null;
            }
        }
        return new ArrayDouble(data);
    }

    /**
     * Convert the list of arguments to a List. Returns null if it's not possible.
     *
     * @param objects
     *            a list of arguments
     * @return the converted list of null
     */
    public static List<String> asListString(List<Object> objects) {
        List<String> data = new ArrayList<>();
        for (var i = 0; i < objects.size(); i++) {
            var value = objects.get(i);
            if (value instanceof String) {
                data.add((String) value);
            } else {
                return null;
            }
        }
        return data;
    }

    /**
     * Parses the string and returns the name of the function plus the list of arguments. The arguments can either be
     * doubles or Strings. Returns null if parsing fails.
     *
     * @param string
     *            the string to be parsed
     * @return the function name and arguments; null if parsing fails
     */
    public static List<Object> parseFunctionAnyParameter(String string) {
        return parseFunctionAnyParameter("(\\w+)", string);
    }

    /**
     * Parses the string and returns the name of the function plus the list of arguments. The arguments can either be
     * doubles or Strings. Returns null if parsing fails.
     *
     * @param nameRegex
     *            the syntax for the function name
     * @param string
     *            the string to be parsed
     * @return the function name and arguments; null if parsing fails
     */
    public static List<Object> parseFunctionAnyParameter(String nameRegex, String string) {
        if (string.indexOf('(') == -1) {
            if (string.matches(nameRegex)) {
                return Arrays.<Object> asList(string);
            } else {
                return null;
            }
        }

        var name = string.substring(0, string.indexOf('('));
        var arguments = string.substring(string.indexOf('(') + 1, string.lastIndexOf(')'));

        if (!name.matches(nameRegex)) {
            return null;
        }

        List<Object> result = new ArrayList<>();
        result.add(name);
        try {
            var parsedArguments = parseCSVLine(arguments.trim(), "\\s*,\\s*");
            if (parsedArguments == null) {
                return null;
            }
            result.addAll(parsedArguments);
            return result;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Parses a line of text representing comma separated values and returns the values themselves.
     *
     * @param line
     *            the line to parse
     * @param separatorRegex
     *            the regular expression for the separator
     * @return the list of values
     */
    private static List<Object> parseCSVLine(String line, String separatorRegex) {
        List<Object> matches = new ArrayList<>();
        var currentPosition = 0;
        var separatorMatcher = Pattern.compile("^" + separatorRegex).matcher(line);
        var stringMatcher = Pattern.compile("^" + QUOTED_STRING_REGEX).matcher(line);
        var doubleMatcher = Pattern.compile("^" + DOUBLE_REGEX).matcher(line);
        while (currentPosition < line.length()) {
            if (stringMatcher.region(currentPosition, line.length()).useAnchoringBounds(true).find()) {
                // Found String match
                var token = line.substring(currentPosition + 1, stringMatcher.end() - 1);
                matches.add(unescapeString(token));
                currentPosition = stringMatcher.end();
            } else if (doubleMatcher.region(currentPosition, line.length()).useAnchoringBounds(true).find()) {
                // Found Double match
                Double token = Double.parseDouble(line.substring(currentPosition, doubleMatcher.end()));
                matches.add(token);
                currentPosition = doubleMatcher.end();
            } else {
                throw new IllegalArgumentException(
                        "Can't parse line: expected token at " + currentPosition + " (" + line + ")");
            }

            if (currentPosition < line.length()) {
                if (!separatorMatcher.region(currentPosition, line.length()).useAnchoringBounds(true).find()) {
                    throw new IllegalArgumentException(
                            "Can't parse line: expected separator at " + currentPosition + " (" + line + ")");
                }
                currentPosition = separatorMatcher.end();
            }
        }
        return matches;
    }

    private static String unescapeString(String escapedString) {
        var match = escapeSequence.matcher(escapedString);
        var output = new StringBuffer();
        while (match.find()) {
            match.appendReplacement(output, substitution(match.group()));
        }
        match.appendTail(output);
        return output.toString();
    }

    private static String substitution(String escapedToken) {
        switch (escapedToken) {
        case "\\\"":
            return "\"";
        case "\\\\":
            return "\\\\";
        case "\\\'":
            return "\'";
        case "\\r":
            return "\r";
        case "\\n":
            return "\n";
        case "\\b":
            return "\b";
        case "\\t":
            return "\t";
        }
        if (escapedToken.startsWith("\\u")) {
            // It seems that you can't use replace with an escaped
            // unicode sequence. Bug in Java?
            // Parsing myself
            return Character.toString((char) Long.parseLong(escapedToken.substring(2), 16));
        }
        return Character.toString((char) Long.parseLong(escapedToken.substring(1), 8));
    }
}
