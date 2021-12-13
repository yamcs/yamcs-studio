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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Definition for a function that can be integrated in the data source language.
 */
public class DSFunction implements Comparable<DSFunction> {

    private final String name;
    private String description;
    private String tooltip;
    private final Class<?> returnType;
    /**
     * Whether the function is a pure function, given the same arguments always returns the same result.
     */
    private final boolean isPure;
    /**
     * Whether the function takes a variable number of arguments.
     * <p>
     * Variable arguments can only be at the end of the argument list, and have the same type.
     */
    private final boolean isVarArgs;
    /**
     * The ordered list of the arguments name.
     */
    private List<String> argumentNames;
    /**
     * The ordered list of the arguments type.
     */
    private List<Class<?>> argumentTypes;

    private List<DSFunction> polymorphicFunctions;

    public DSFunction(String name, Class<?> returnType, boolean isPure, boolean isVarArgs) {
        this.name = name;
        this.returnType = returnType;
        this.isPure = isPure;
        this.isVarArgs = isVarArgs;
        argumentNames = new ArrayList<>();
        argumentTypes = new ArrayList<>();
        polymorphicFunctions = new ArrayList<>();
    }

    public void addPolymorphicFunction(DSFunction function) {
        polymorphicFunctions.add(function);
    }

    public boolean isPolymorphic() {
        return polymorphicFunctions.size() > 0;
    }

    public void addArgument(String name, Class<?> type) {
        argumentNames.add(name);
        argumentTypes.add(type);
    }

    public int getNbArgs() {
        return argumentNames.size();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getName() {
        return name;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public boolean isPure() {
        return isPure;
    }

    public boolean isVarArgs() {
        return isVarArgs;
    }

    public List<String> getArgumentNames() {
        return argumentNames;
    }

    public List<Class<?>> getArgumentTypes() {
        return argumentTypes;
    }

    public List<DSFunction> getPolymorphicFunctions() {
        return polymorphicFunctions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        var other = (DSFunction) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(DSFunction arg0) {
        return name.compareTo(arg0.getName());
    }
}
