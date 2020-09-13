/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.yamcs.studio.data.formula.alarm.AlarmFunctionSet;
import org.yamcs.studio.data.formula.array.ArrayFunctionSet;
import org.yamcs.studio.data.formula.math.MathFunctionSet;
import org.yamcs.studio.data.formula.venum.VEnumFunctionSet;
import org.yamcs.studio.data.formula.vnumber.VNumberFunctionSet;
import org.yamcs.studio.data.formula.vstring.VStringFunctionSet;
import org.yamcs.studio.data.formula.vtable.VTableFunctionSet;
import org.yamcs.studio.data.vtype.VType;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * The registry to add functions that will be used by the formula parser.
 */
public class FormulaRegistry {
    private final static FormulaRegistry registry = new FormulaRegistry();

    static {
        registry.registerFormulaFunctionSet(new ArrayFunctionSet());
        registry.registerFormulaFunctionSet(new MathFunctionSet());
        registry.registerFormulaFunctionSet(new VStringFunctionSet());
        registry.registerFormulaFunctionSet(new VTableFunctionSet());
        registry.registerFormulaFunctionSet(new VNumberFunctionSet());
        registry.registerFormulaFunctionSet(new AlarmFunctionSet());
        registry.registerFormulaFunctionSet(new VEnumFunctionSet());
    }

    /**
     * Returns the default formula registry.
     *
     * @return the default registry
     */
    public static FormulaRegistry getDefault() {
        return registry;
    }

    /**
     * Registers a formula set.
     *
     * @param functionSet
     *            a formula set
     */
    public void registerFormulaFunctionSet(FormulaFunctionSet functionSet) {
        functionSets.put(functionSet.getName(), functionSet);
        for (FormulaFunction formulaFunction : functionSet.getFunctions()) {
            registerFormulaFunction(formulaFunction);
        }
    }

    private final Map<String, FormulaFunctionSet> functionSets = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, Collection<FormulaFunction>>> formulaFunctions = new ConcurrentHashMap<>();
    private final Map<String, VType> constants = new ConcurrentHashMap<>();

    {
        constants.put("TRUE", ValueFactory.toVType(true));
        constants.put("FALSE", ValueFactory.toVType(false));
        constants.put("PI", ValueFactory.toVType(Math.PI));
        constants.put("E", ValueFactory.toVType(Math.E));
    }

    private void registerFormulaFunction(FormulaFunction formulaFunction) {
        // Get the map based by name
        Map<Integer, Collection<FormulaFunction>> functionForName = formulaFunctions.get(formulaFunction.getName());
        if (functionForName == null) {
            functionForName = new ConcurrentHashMap<>();
            formulaFunctions.put(formulaFunction.getName(), functionForName);
        }

        // Get the collection based on number of arguments
        Collection<FormulaFunction> functionsForNArguments = functionForName
                .get(formulaFunction.getArgumentNames().size());
        if (functionsForNArguments == null) {
            functionsForNArguments = Collections.newSetFromMap(new ConcurrentHashMap<FormulaFunction, Boolean>());
            functionForName.put(formulaFunction.getArgumentNames().size(), functionsForNArguments);
        }

        // Add formula
        functionsForNArguments.add(formulaFunction);
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
    public FormulaFunctionSet findFunctionSet(String name) {
        return functionSets.get(name);
    }

    /**
     * Finds the registered function with the given name and that can accept the given number of arguments.
     *
     * @param functionName
     *            the name of the function
     * @param nArguments
     *            the number of the arguments
     * @return the matched functions
     */
    public Collection<FormulaFunction> findFunctions(String functionName, Integer nArguments) {
        Set<FormulaFunction> functions = new HashSet<>();
        for (FormulaFunctionSet formulaFunctionSet : functionSets.values()) {
            for (FormulaFunction formulaFunction : formulaFunctionSet.getFunctions()) {
                if (formulaFunction.getName().equals(functionName) &&
                        FormulaFunctions.matchArgumentCount(nArguments, formulaFunction)) {
                    functions.add(formulaFunction);
                }
            }
        }

        return functions;
    }

    /**
     * Returns the value of the constant with the given name
     *
     * @param constantName
     *            the constant name
     * @return the value of the name
     */
    public VType findNamedConstant(String constantName) {
        return constants.get(constantName);
    }
}
