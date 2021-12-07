package org.yamcs.studio.editor.base.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.formula.FormulaFunctionSet;

public class FunctionTreeContentProvider implements ITreeContentProvider {

    private List<FormulaFunctionSet> functionSets;

    @Override
    @SuppressWarnings("unchecked")
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.functionSets = (List<FormulaFunctionSet>) newInput;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return functionSets.toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof FormulaFunctionSet) {
            List<FormulaFunction> functions = new ArrayList<>(((FormulaFunctionSet) parentElement).getFunctions());
            Collections.sort(functions, (o1, o2) -> {
                var result = o1.getName().compareTo(o2.getName());
                if (result != 0) {
                    return result;
                }
                return Integer.compare(o1.getArgumentTypes().size(), o2.getArgumentTypes().size());
            });
            return functions.toArray();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof FormulaFunctionSet) {
            return !((FormulaFunctionSet) element).getFunctions().isEmpty();
        }
        return false;
    }

    @Override
    public void dispose() {
    }
}
