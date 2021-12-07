/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * The utility functions for macros operations.
 */
public class MacroUtil {

    private static final String MACRO_RIGHT_PART = "[)}]";

    private static final String MACRO_LEFT_PART = "\\$[{(]";

    /**
     * Replace macros in String.
     * 
     * @param input
     *            the input string to be parsed
     * @param macroTableProvider
     *            the macro table provider
     * @return
     * @throws InfiniteLoopException
     *             when infinite loop is detected. For example, for a macro table "a=$(b), b=$(a)", this string "$(a)"
     *             will result in infinite loop.
     */
    public static String replaceMacros(String input, IMacroTableProvider macroTableProvider)
            throws InfiniteLoopException {
        return replaceMacros(input, macroTableProvider, new HashSet<String>(), false);
    }

    /**
     * Detect macro 'start brace', round or curly
     *
     * @param character
     *            Test character
     * @return True if '(' or '{', otherwise False
     */
    private static boolean isStart(char character) {
        return character == '(' || character == '{';
    }

    /**
     * Detect macro 'end brace', round or curly
     *
     * @param character
     *            Test character
     * @return True if ')' or '}', otherwise False
     */
    private static boolean isEnd(char character) {
        return character == ')' || character == '}';
    }

    /**
     * Replace macros in String.
     * 
     * @param input
     *            the input string to be parsed
     * @param macroTableProvider
     *            the macro table provider
     * @return
     * @throws InfiniteLoopException
     *             when infinite loop is detected. For example, for a macro table "a=$(b), b=$(a)", this string "$(a)"
     *             will result in infinite loop.
     */
    private static String replaceMacros(String input, IMacroTableProvider macroTableProvider, Set<String> parsedMacros,
            boolean insideParse) throws InfiniteLoopException {
        // if there is no macro in the input, return
        if (!input.contains("$")) {
            return input;
        }
        var stringBuilder = new StringBuilder();
        var stack = new Stack<Integer>();
        var lockStack = false; // lock the stack to prevent pushing new element
        var scanPosition = 0;
        for (var i = 0; i < input.length(); i++) {
            if (!lockStack) {
                if (input.charAt(i) == '$' && i < input.length() - 1 && MacroUtil.isStart(input.charAt(i + 1))) {
                    stack.push(i);
                    continue;
                }
            }
            if (stack.size() > 0 && MacroUtil.isEnd(input.charAt(i))) {
                try {
                    lockStack = true; // lock the stack until it is popped out.
                    int start = stack.pop();
                    if (stack.size() == 0) { // arrived the most out, we got a macro
                        var macro = input.substring(start, i + 1);
                        if (!insideParse) {
                            parsedMacros.clear();
                        }
                        var macroValue = parseMacro(macro, macroTableProvider, parsedMacros, insideParse);
                        stringBuilder.append(input.substring(scanPosition, start) + macroValue);
                        scanPosition = i + 1;
                        lockStack = false;
                    }
                } catch (EmptyStackException e) {
                    lockStack = false;
                }

            }
        }
        // if there is more chars behind the last macro
        if (scanPosition < input.length()) {
            stringBuilder.append(input.substring(scanPosition));
        }
        return stringBuilder.toString();
    }

    /**
     * Parse a macro unit(${...}) and replace the macro with value from provider. It supports recursive macros, for
     * example ${${...}}.
     * 
     * @param input
     *            the input macro unit which has a format like ${...} or $(...)
     * @param macroTableProvider
     *            the macro table provider
     * @param parsedMacros
     *            the parsed macros history in the recursive stack.
     * @return the result of parsing.
     * @throws InfiniteLoopException
     */
    private static String parseMacro(String input, IMacroTableProvider macroTableProvider, Set<String> parsedMacros,
            boolean insideParse) throws InfiniteLoopException {
        // if there is no macro in the input, return
        if (!input.matches(MACRO_LEFT_PART + ".+" + MACRO_RIGHT_PART)) {
            return input;
        }
        var result = input;

        var innerStart = -1;
        for (var i = 0; i < input.length(); i++) {

            if (input.charAt(i) == '$' && i < input.length() - 1 && MacroUtil.isStart(input.charAt(i + 1))) {
                innerStart = i;
                continue;
            }

            if (MacroUtil.isEnd(input.charAt(i))) {
                if (innerStart == -1) {
                    return result;
                }

                var macroName = input.substring(innerStart + 2, i);
                // if it has been parsed before, stop parse to prevent infinite loop
                if (!parsedMacros.add(macroName)) {
                    throw new InfiniteLoopException("Infinite loop was detected when parsing the macro: " + macroName);
                }

                var macroValue = macroTableProvider.getMacroValue(macroName);
                if (macroValue == null) {
                    return result;
                }

                result = input.substring(0, innerStart) + macroValue + input.substring(i + 1);
                return replaceMacros(result, macroTableProvider, parsedMacros, true);

            }
        }
        return result;
    }

}
