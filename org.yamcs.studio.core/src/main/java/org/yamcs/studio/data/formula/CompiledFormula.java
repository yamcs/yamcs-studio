/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yamcs.studio.data.vtype.IVDouble;
import org.yamcs.studio.data.vtype.IVInt;
import org.yamcs.studio.data.vtype.IVString;
import org.yamcs.studio.data.vtype.VType;

public class CompiledFormula {

    private FormulaAst rootNode;
    private Map<String, VType> inputValues = new HashMap<>();
    private FormulaRegistry registry = FormulaRegistry.getDefault();

    public CompiledFormula(String formulaString) {
        rootNode = FormulaAst.formula(formulaString);
    }

    public void updateInput(String pvName, VType value) {
        inputValues.put(pvName, value);
    }

    public List<String> getDependencies() {
        return rootNode.listChannelNames();
    }

    public Object execute() {
        return executeExpression(rootNode);
    }

    private Object executeExpression(FormulaAst node) {
        switch (node.getType()) {
        case OP:
            var func = (String) node.getValue();
            return executeOP(func, node.getChildren());
        case INTEGER:
            return new IVInt((Integer) node.getValue());
        case FLOATING_POINT:
            return new IVDouble((Double) node.getValue());
        case STRING:
            return new IVString((String) node.getValue());
        case CHANNEL:
            var channelName = (String) node.getValue();
            return inputValues.get(channelName);
        case ID:
            var id = (String) node.getValue();
            return registry.findNamedConstant(id);
        default:
            throw new IllegalStateException("Unexpected node type " + node.getType());
        }
    }

    private Object executeOP(String func, List<FormulaAst> argNodes) {
        var cardinality = argNodes.size();
        var functions = registry.findFunctions(func, cardinality);

        var argumentValues = new ArrayList<>(cardinality);
        for (var argNode : argNodes) {
            argumentValues.add(executeExpression(argNode));
        }

        var func2 = FormulaFunctions.findFirstMatch(argumentValues, functions);
        return func2.calculate(argumentValues);
    }
}
