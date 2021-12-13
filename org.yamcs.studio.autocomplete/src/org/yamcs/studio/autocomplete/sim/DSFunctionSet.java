/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.sim;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * A set of functions that can be used in the data source.
 * <p>
 * Objects of this class can be registered in the {@link DSFunctionRegistry} and the functions will be available in the
 * data source language.
 */
public class DSFunctionSet {

    public static Pattern namePattern = Pattern.compile("[a-zA-Z_]\\w*");

    private String name;
    private String description;
    private Collection<DSFunction> functions;

    /**
     * Creates a new set of functions to be registered in the data source language.
     *
     * @param functionSetDescription
     *            the description of the function set
     */
    public DSFunctionSet(DSFunctionSetDescription functionSetDescription) {
        this.name = functionSetDescription.name;
        this.description = functionSetDescription.description;
        this.functions = Collections.unmodifiableSet(new TreeSet<>(functionSetDescription.functions));
    }

    /**
     * Returns the name of the function set.
     *
     * @return the function set name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the description of the function set.
     *
     * @return the function set description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * The names of all functions in this set.
     *
     * @return the function names
     */
    public final Collection<String> getFunctionNames() {
        var names = new HashSet<String>();
        for (var formulaFunction : functions) {
            names.add(formulaFunction.getName());
        }
        return names;
    }

    /**
     * Returns all the functions in the set with the given name.
     *
     * @param name
     *            the name of the function
     * @return the matched functions; never null
     */
    public final Collection<DSFunction> findFunctions(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        var formulas = new HashSet<DSFunction>();
        for (var formulaFunction : functions) {
            if (name.equals(formulaFunction.getName())) {
                formulas.add(formulaFunction);
            }
        }
        return formulas;
    }

    /**
     * Returns all functions in the set.
     *
     * @return the functions in the set
     */
    public final Collection<DSFunction> getFunctions() {
        return functions;
    }
}
