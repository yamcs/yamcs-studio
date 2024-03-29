/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.sim;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class to parse variable names and create simulated signals.
 */
public class NameParser {

    static final Pattern doubleParameter = Pattern.compile("\\s*([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)\\s*");
    static final Pattern commaSeparatedDoubles = Pattern.compile(doubleParameter + "(," + doubleParameter + ")*");
    static final Pattern functionAndParameter = Pattern.compile("(\\w+)(\\(((" + commaSeparatedDoubles + ")?)\\))?");
    static final Pattern functionAndStringParameter = Pattern.compile("(\\w+)(\\((\".*\")\\))?");

    /**
     * Parses a comma separated list of arguments and returns them as a list.
     *
     * @param string
     *            a comma separated list of arguments; if null or empty returns the empty list
     * @return the list of parsed arguments
     */
    static List<Object> parseParameters(String string) {
        // Argument is empty
        if (string == null || "".equals(string)) {
            return Collections.emptyList();
        }

        // Validate input
        if (!commaSeparatedDoubles.matcher(string).matches()) {
            throw new IllegalArgumentException(
                    "Arguments must be a comma separated list of double values (was " + string + ")");
        }

        // Parse parameters
        var matcher = doubleParameter.matcher(string);
        List<Object> parameters = new ArrayList<>();
        while (matcher.find()) {
            var parameter = matcher.group();
            Double value = Double.parseDouble(parameter);
            parameters.add(value);
        }

        return parameters;
    }

    /**
     * Parse a function with parameters and returns a list where the first element is the function name and the others
     * are the parsed arguments.
     *
     * @param string
     *            a string representing a function
     * @return the name and the parameters
     */
    static List<Object> parseFunction(String string) {
        var matcher = functionAndParameter.matcher(string);
        // Match comma separate double list
        if (matcher.matches()) {
            List<Object> parameters = new ArrayList<>();
            parameters.add(matcher.group(1));
            parameters.addAll(parseParameters(matcher.group(3)));
            return parameters;
        }

        // Match string parameter
        matcher = functionAndStringParameter.matcher(string);
        if (matcher.matches()) {
            List<Object> parameters = new ArrayList<>();
            parameters.add(matcher.group(1));
            var quotedString = matcher.group(3);
            parameters.add(quotedString.substring(1, quotedString.length() - 1));
            return parameters;
        }

        throw new IllegalArgumentException(
                "Syntax error: function should be like xxx(num1, num2, ...) or xxx(\"string\") and was " + string);
    }

    /**
     * Given a string representing a function call, finds the appropriate call matching the function name, and the
     * appropriate constructor and instantiates it.
     *
     * @param string
     *            the function call
     * @return the function
     */
    public static SimFunction<?> createFunction(String string) {
        var parameters = parseFunction(string);
        var className = new StringBuilder("org.yamcs.studio.data.sim.");
        var firstCharPosition = className.length();
        className.append((String) parameters.get(0));
        className.setCharAt(firstCharPosition, Character.toUpperCase(className.charAt(firstCharPosition)));

        try {
            @SuppressWarnings("unchecked")
            var clazz = (Class<SimFunction<?>>) Class.forName(className.toString());
            var constructorParams = parameters.subList(1, parameters.size()).toArray();
            var types = new Class[constructorParams.length];
            for (var i = 0; i < types.length; i++) {
                types[i] = constructorParams[i].getClass();
            }
            return clazz.getConstructor(types).newInstance(constructorParams);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Simulation channel " + parameters.get(0) + " is not defined");
        } catch (NoClassDefFoundError ex) {
            if (ex.getMessage().contains("wrong name") && ex.getMessage().lastIndexOf("/") != -1) {
                var suggestedName = ex.getMessage().substring(ex.getMessage().lastIndexOf("/") + 1,
                        ex.getMessage().length() - 1);
                throw new RuntimeException(
                        "Function " + parameters.get(0) + " is not defined (Looking for " + suggestedName + "?)");
            }
            throw new RuntimeException("Function " + parameters.get(0) + " is not defined");
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Wrong parameter number for function " + parameters.get(0));
        } catch (SecurityException ex) {
            throw new RuntimeException("Constructor for " + parameters.get(0) + " should be at least package private");
        } catch (InstantiationException ex) {
            throw new RuntimeException("Constructor for " + parameters.get(0) + " failed", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Constructor for " + parameters.get(0) + " should be at least package private");
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Wrong parameter type for function " + parameters.get(0));
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause().getMessage(), ex);
        }
    }
}
