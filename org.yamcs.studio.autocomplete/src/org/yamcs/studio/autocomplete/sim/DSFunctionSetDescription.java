/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.sim;

import java.util.Collection;
import java.util.HashSet;

/**
 * The description for a function set to be used in data source language.
 */
public class DSFunctionSetDescription {

    String name;
    String description;
    Collection<DSFunction> functions = new HashSet<>();

    /**
     * A new function set description.
     *
     * @param name
     *            the name of the function set
     * @param description
     *            the description of the function set
     */
    public DSFunctionSetDescription(String name, String description) {
        this.name = name;
        this.description = description;
        if (!DSFunctionSet.namePattern.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Name must start by a letter and only consist of letters and numbers");
        }
    }

    /**
     * Adds a function in the set.
     *
     * @param function
     *            the function to add
     * @return this description
     */
    public DSFunctionSetDescription addFunction(DSFunction function) {
        functions.add(function);
        return this;
    }
}
