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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The registry to add functions that will be used by the auto-complete.
 */
public class DSFunctionRegistry {

    private final static DSFunctionRegistry registry = new DSFunctionRegistry();

    /**
     * Returns the default data source function registry.
     *
     * @return the default registry
     */
    public static DSFunctionRegistry getDefault() {
        return registry;
    }

    private Map<String, DSFunctionSet> functionSets = new ConcurrentHashMap<>();

    /**
     * Registers a formula set.
     *
     * @param functionSet
     *            a formula set
     */
    public void registerDSFunctionSet(DSFunctionSet functionSet) {
        functionSets.put(functionSet.getName(), functionSet);
    }

    /**
     * Returns the names of all the registered function sets.
     *
     * @return the names of the registered function sets
     */
    public Set<String> listFunctionSets() {
        return Collections.unmodifiableSet(new HashSet<>(functionSets.keySet()));
    }

    /**
     * Returns the registered function set with the given name.
     *
     * @param name
     *            the function set name
     * @return the set or null
     */
    public DSFunctionSet findFunctionSet(String name) {
        return functionSets.get(name);
    }
}
