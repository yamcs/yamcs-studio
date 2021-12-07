/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.displays;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class ParameterTableContentProvider implements IStructuredContentProvider {

    private TableViewer tableViewer;
    private List<String> parameters = new ArrayList<>();

    public ParameterTableContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return parameters.toArray();
    }

    public boolean addParameter(String info) {
        if (parameters.contains(info)) {
            return false;
        }
        parameters.add(info);
        return true;
    }

    public void clearAll() {
        parameters.clear();
        tableViewer.getTable().clearAll();
    }

    public void remove(Object info) {
        parameters.remove(info);
    }

    public boolean hasChanged() {
        return false;
    }
}
