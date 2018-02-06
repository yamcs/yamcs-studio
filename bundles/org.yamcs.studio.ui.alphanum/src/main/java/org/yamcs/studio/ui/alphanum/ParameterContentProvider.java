package org.yamcs.studio.ui.alphanum;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.yamcs.protobuf.Mdb.ParameterInfo;

public class ParameterContentProvider implements IStructuredContentProvider {

    TableViewer table;
    List<ParameterInfo> parameter;
    List<ParameterInfo> initial;

    public List<ParameterInfo> getParameter() {
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

    public void load(List<ParameterInfo> list) {
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

    public boolean addParameter(ParameterInfo info) {
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

    public List<ParameterInfo> getInitial() {
        return initial;
    }

    public boolean hasChanged() {
        return initial.size() != parameter.size() || !initial.containsAll(parameter);
    }

}
