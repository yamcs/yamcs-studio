package org.yamcs.studio.editor.base.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.csstudio.csdata.ProcessVariable;
import org.csstudio.ui.util.dnd.ControlSystemDragSource;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.studio.core.ui.XtceSubSystemNode;
import org.yamcs.xtce.Parameter;
import org.yamcs.xtce.SpaceSystem;
import org.yamcs.xtce.xml.XtceStaxReader;

public class ParametersView extends ViewPart {

    private TreeViewer viewer;
    private ParametersContentProvider contentProvider;

    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        contentProvider = new ParametersContentProvider();
        viewer.setContentProvider(contentProvider);
        viewer.setInput(contentProvider);
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(true);

        enableDragAndDrop();

        TreeViewerColumn nameColumn = new TreeViewerColumn(viewer, SWT.NONE);
        nameColumn.getColumn().setWidth(300);
        nameColumn.getColumn().setText("Parameter");
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceParameterNode) {
                    return ((XtceParameterNode) element).getName();
                } else {
                    return ((XtceSubSystemNode) element).getName();
                }
            }
        });

        GridLayoutFactory.fillDefaults().generateLayout(parent);

        viewer.getTree().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem item = (TreeItem) e.item;
                if (item != null && item.getItemCount() > 0) {
                    item.setExpanded(!item.getExpanded());
                    viewer.refresh();
                }
            }
        });

        viewer.addDoubleClickListener(event -> {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            Object selectedNode = sel.getFirstElement();
            viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
        });

        getSite().setSelectionProvider(viewer);

        // Set initial state
        viewer.refresh();
    }

    private void enableDragAndDrop() {
        new ControlSystemDragSource(viewer.getControl()) {

            @Override
            public Object getSelection() {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                Object[] objs = selection.toArray();
                List<ProcessVariable> pvs = new ArrayList<>();
                for (Object obj : objs) {
                    if (obj instanceof XtceParameterNode) {
                        XtceParameterNode pNode = (XtceParameterNode) obj;
                        pvs.add(new ProcessVariable(pNode.getParameter().getQualifiedName()));
                    }
                }
                return pvs.toArray(new ProcessVariable[0]);
            }
        };
    }

    public void importFile(File file) throws XMLStreamException, IOException {
        XtceStaxReader xtceReader = new XtceStaxReader();
        SpaceSystem spaceSystem = xtceReader.readXmlDocument(file.getAbsolutePath());
        spaceSystem.setQualifiedName("/" + spaceSystem.getName());
        loadParameters(spaceSystem);
        viewer.refresh();
    }

    private void loadParameters(SpaceSystem spaceSystem) {
        for (SpaceSystem subSystem : spaceSystem.getSubSystems()) {
            String qname = spaceSystem.getQualifiedName() + "/" + subSystem.getName();
            subSystem.setQualifiedName(qname);
            loadParameters(subSystem);
        }
        for (Parameter subParameter : spaceSystem.getParameters()) {
            String qname = spaceSystem.getQualifiedName() + "/" + subParameter.getName();
            subParameter.setQualifiedName(qname);
            contentProvider.addElement(qname, subParameter);
        }
    }

    @Override
    public void setFocus() {
        viewer.getTree().setFocus();
    }
}
