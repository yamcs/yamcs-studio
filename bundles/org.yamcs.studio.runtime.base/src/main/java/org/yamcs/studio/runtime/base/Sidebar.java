package org.yamcs.studio.runtime.base;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class Sidebar extends Composite {

    private TreeViewer viewer;

    public Sidebar(Composite parent, int style) {
        super(parent, style);
        setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));

        Composite treeWrapper = new Composite(this, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        treeWrapper.setLayoutData(gd);
        TreeColumnLayout tcl = new TreeColumnLayout();
        treeWrapper.setLayout(tcl);

        viewer = new TreeViewer(treeWrapper, SWT.SINGLE);
        viewer.setContentProvider(new ActionTreeContentProvider());
        viewer.getTree().setHeaderVisible(false);
        viewer.getTree().setLinesVisible(true);

        TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
        viewerColumn.getColumn().setWidth(300);
        tcl.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(1000, false));
        viewerColumn.setLabelProvider(new ColumnLabelProvider());

        viewer.setInput(new String[] { "Alarms", "Events", "MDB", "Commanding" });
    }

    @Override
    public boolean setFocus() {
        return viewer.getTree().setFocus();
    }
}
