package org.yamcs.studio.alphanumeric;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class ParameterContentProvider implements IStructuredContentProvider {

    TableViewer table;
    List<String> parameter;
    List<String> initial;

    public List<String> getParameter() {
        return parameter;
    }

    public ParameterContentProvider(TableViewer parameterTableViewer) {
        table = parameterTableViewer;
        parameter = new ArrayList<>();
        initial = new ArrayList<>();
    }

    @Override
    public void dispose() {


    }

    public void load(List<String> list) {
        initial = new ArrayList<>();
        initial.addAll(list);
    }


    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object[] getElements(Object inputElement) {
        return parameter.toArray();
    }

    public boolean addParameter(String info) {
        if(parameter.contains(info))
            return false;
        parameter.add(info);
        return true;
    }

    public void clearAll() {
        parameter.clear();
        table.getTable().clearAll();

    }

    public void restore() {
        parameter.clear();
        parameter.addAll(initial);

    }

    public void remove(Object info) {
        parameter.remove(info);

    }

    public List<String> getInitial() {
        return initial;
    }

    public boolean hasChanged() {
        return initial.size() != parameter.size() || !initial.containsAll(parameter);
    }

}
