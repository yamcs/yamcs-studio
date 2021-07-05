package org.yamcs.studio.data.formula;

import java.util.ArrayList;
import java.util.Collection;
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
        this.rootNode = FormulaAst.formula(formulaString);
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
            String func = (String) node.getValue();
            return executeOP(func, node.getChildren());
        case INTEGER:
            return new IVInt((Integer) node.getValue());
        case FLOATING_POINT:
            return new IVDouble((Double) node.getValue());
        case STRING:
            return new IVString((String) node.getValue());
        case CHANNEL:
            String channelName = (String) node.getValue();
            return inputValues.get(channelName);
        case ID:
            String id = (String) node.getValue();
            return registry.findNamedConstant(id);
        default:
            throw new IllegalStateException("Unexpected node type " + node.getType());
        }
    }

    private Object executeOP(String func, List<FormulaAst> argNodes) {
        int cardinality = argNodes.size();
        Collection<FormulaFunction> functions = registry.findFunctions(func, cardinality);

        List<Object> argumentValues = new ArrayList<>(cardinality);
        for (FormulaAst argNode : argNodes) {
            argumentValues.add(executeExpression(argNode));
        }

        FormulaFunction func2 = FormulaFunctions.findFirstMatch(argumentValues, functions);
        return func2.calculate(argumentValues);
    }
}
