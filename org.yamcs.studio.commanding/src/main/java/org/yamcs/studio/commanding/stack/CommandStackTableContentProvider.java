/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class CommandStackTableContentProvider implements IStructuredContentProvider {

    private CommandStack stack = CommandStack.getInstance();
    private TableViewer tableViewer;

    public CommandStackTableContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO ?
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return stack.getCommands().toArray();
    }

    public int indexOf(Object element) {
        return stack.getCommands().indexOf(element);
    }

    public void addTelecommand(StackedCommand entry) {
        stack.addCommand(entry);
        tableViewer.add(entry);
    }

    public void insertTelecommand(StackedCommand entry, int index) {
        stack.insertCommand(entry, index);
        tableViewer.insert(entry, index);
    }
}
