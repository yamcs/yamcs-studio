/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.diirt.util.config.ServiceLoaderOSGiWrapper;
import org.diirt.vtype.ValueFactory;

/**
 * The registry to add functions that will be used by the formula parser.
 *
 * @author carcassi
 */
public class FormulaRegistry {
    private final static FormulaRegistry registry = new FormulaRegistry();

    private static final Logger log = Logger.getLogger(FormulaRegistry.class.getName());

    static {
        // Find formula functions to register using the ServiceLoader
        ServiceLoaderOSGiWrapper.load(FormulaFunctionSet.class, log, registry::registerFormulaFunctionSet);
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
     * @param functionSet a formula set
     */
    public void registerFormulaFunctionSet(FormulaFunctionSet functionSet) {
        functionSets.put(functionSet.getName(), functionSet);
        for (FormulaFunction formulaFunction : functionSet.getFunctions()) {
            registerFormulaFunction(formulaFunction);
        }
    }

    private final Map<String, FormulaFunctionSet> functionSets = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, Collection<FormulaFunction>>> formulaFunctions = new ConcurrentHashMap<>();
    private final Map<String, Object> constants = new ConcurrentHashMap<>();

    {
        constants.put("TRUE", ValueFactory.toVType(true));
        constants.put("FALSE", ValueFactory.toVType(false));
        constants.put("PI", ValueFactory.toVType(Math.PI));
        constants.put("E", ValueFactory.toVType(Math.E));
    }

    private void registerFormulaFunction(FormulaFunction formulaFunction) {
        validateFormulaFunction(formulaFunction);

        // Get the map based by name
        Map<Integer, Collection<FormulaFunction>> functionForName = formulaFunctions.get(formulaFunction.getName());
        if (functionForName == null) {
            functionForName = new ConcurrentHashMap<>();
            formulaFunctions.put(formulaFunction.getName(), functionForName);
        }

        // Get the collection based on number of arguments
        Collection<FormulaFunction> functionsForNArguments = functionForName.get(formulaFunction.getArgumentNames().size());
        if (functionsForNArguments == null) {
            functionsForNArguments = Collections.newSetFromMap(new ConcurrentHashMap<FormulaFunction, Boolean>());
            functionForName.put(formulaFunction.getArgumentNames().size(), functionsForNArguments);
        }

        // Add formula
        functionsForNArguments.add(formulaFunction);
    }

    static void validateFormulaFunction(FormulaFunction formulaFunction)  {
        if (formulaFunction instanceof StatefulFormulaFunction) {
            StatefulFormulaFunction function = (StatefulFormulaFunction) formulaFunction;
            StatefulFormulaFunction instance;
            try {
                instance = function.getClass().newInstance();
            } catch (SecurityException | InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException("StatefulFormulaFunction " + formulaFunction.getClass().getName() + " must be public and have a public no-arg constructor.", ex);
            }
            try {
                instance.dispose();
            } catch (Exception ex) {
                throw new RuntimeException("StatefulFormulaFunction " + formulaFunction.getClass().getName() + " fails on dispose.", ex);
            }

        }
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
     * @param name the function set name
     * @return the set or null
     */
    public FormulaFunctionSet findFunctionSet(String name) {
        return functionSets.get(name);
    }

    /**
     * Finds the registered function with the given name and that can
     * accept the given number of arguments.
     *
     * @param functionName the name of the function
     * @param nArguments the number of the arguments
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
     * @param constantName the constant name
     * @return the value of the name
     */
    public Object findNamedConstant(String constantName) {
        return constants.get(constantName);
    }
}
