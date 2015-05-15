package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class CommandStackTableContentProvider implements IStructuredContentProvider {

    private List<Telecommand> records = new ArrayList<>();
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
        return records.toArray();
    }

    public int indexOf(Object element) {
        return records.indexOf(element);
    }

    public void addTelecommand(Telecommand entry) {
        records.add(entry);
        tableViewer.add(entry);
    }
}
