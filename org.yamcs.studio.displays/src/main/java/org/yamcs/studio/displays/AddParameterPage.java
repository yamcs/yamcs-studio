package org.yamcs.studio.displays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.YamcsPlugin;

public class AddParameterPage extends WizardPage {

    public static final String COL_NAMESPACE = "Namespace";
    public static final String COL_NAME = "Name";
    public static final int COLUMN_WIDTH = 10;
    public static final int COLUMN_MAX_WIDTH = 600;

    private TreeViewer treeViewer;
    private TableViewer tableViewer;
    private TableColumnLayout tcl;
    private TreeColumnLayout trcl;
    private List<ParameterInfo> selectedParameters;
    private ParameterContentProvider contentProvider;

    private Map<String, ArrayList<ParameterInfo>> parameterInfos;

    public AddParameterPage() {
        super("Choose parameters");
        setTitle("Choose Parameters");
        parameterInfos = new HashMap<>();
    }

    @Override
    public void createControl(Composite parent) {
        var composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        var gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.numColumns = 2;
        gl.makeColumnsEqualWidth = false;
        composite.setLayout(gl);

        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        var tableWrapper1 = new Composite(composite, SWT.NONE);
        contentProvider = new ParameterContentProvider();
        trcl = new TreeColumnLayout();
        tableWrapper1.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableWrapper1.setLayout(trcl);

        treeViewer = new TreeViewer(tableWrapper1, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.getTree().setLinesVisible(true);

        // column container
        var pathColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        pathColumn.getColumn().setText(COL_NAMESPACE);
        pathColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var namespace = (String) element;
                return namespace.substring(namespace.lastIndexOf("/") + 1);
            }
        });
        trcl.setColumnData(pathColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));

        treeViewer.addSelectionChangedListener(evt -> {
            var sel = (IStructuredSelection) evt.getSelection();
            if (sel.isEmpty()) {
                contentProvider.setNamespace(null);
                return;
            }
            contentProvider.setNamespace((String) sel.getFirstElement());

        });

        treeViewer.setContentProvider(new NamespaceContentProvider());

        treeViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                var n1 = (String) o1;
                var n2 = (String) o2;
                return n1.compareTo(n2);

            }
        });

        var tableWrapper2 = new Composite(composite, SWT.NONE);
        tcl = new TableColumnLayout();
        tableWrapper2.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableWrapper2.setLayout(tcl);

        tableViewer = new TableViewer(tableWrapper2, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);

        var nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        nameColumn.getColumn().setText(COL_NAME);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var obj = (ParameterInfo) element;
                return obj.getQualifiedName();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));

        tableViewer.addSelectionChangedListener(evt -> {
            var sel = (IStructuredSelection) evt.getSelection();
            if (sel.isEmpty()) {
                setParameter(new ArrayList<>());
                return;
            }
            List<ParameterInfo> parameters = new ArrayList<>();
            for (Object obj : sel.toArray()) {
                parameters.add((ParameterInfo) obj);
            }

            setParameter(parameters);
            setPageComplete(true);

        });

        tableViewer.setContentProvider(contentProvider);
        tableViewer.setInput(contentProvider);

        YamcsPlugin.getMissionDatabase().getParameters().forEach(pmtr -> {

            for (NamedObjectId alias : pmtr.getAliasList()) {
                var namespace = alias.getNamespace();
                if (!namespace.startsWith("/")) {
                    return;
                }
                if (!parameterInfos.containsKey(namespace)) {
                    parameterInfos.put(namespace, new ArrayList<>());
                }

                parameterInfos.get(namespace).add(pmtr);

                var parentns = namespace.substring(0, namespace.lastIndexOf("/"));
                while (!parentns.isEmpty()) {
                    if (!parameterInfos.containsKey(parentns)) {
                        parameterInfos.put(parentns, new ArrayList<>());
                    }
                    parentns = parentns.substring(0, parentns.lastIndexOf("/"));

                }

            }

        });
        treeViewer.setInput(parameterInfos.keySet());

        tableViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                var n1 = (ParameterInfo) o1;
                var n2 = (ParameterInfo) o2;
                return n1.getQualifiedName().compareTo(n2.getQualifiedName());

            }
        });

    }

    private void setParameter(List<ParameterInfo> elements) {
        selectedParameters = elements;
    }

    public List<ParameterInfo> getParameter() {
        return selectedParameters;
    }

    private class ParameterContentProvider implements IStructuredContentProvider {

        private String namespace;

        @Override
        public void dispose() {
            // TODO Auto-generated method stub

        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // TODO Auto-generated method stub

        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (namespace == null) {
                return new String[0];
            }

            return parameterInfos.get(namespace).toArray();
        }

        private void setNamespace(String namespace) {
            this.namespace = namespace;
            tableViewer.refresh();
        }

    }

    private class NamespaceContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
            // TODO Auto-generated method stub

        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // TODO Auto-generated method stub

        }

        @Override
        public Object[] getElements(Object inputElement) {

            List<String> elements = new ArrayList<>();
            for (String name : parameterInfos.keySet()) {
                if (getParent(name) == null) {
                    elements.add(name);
                }
            }

            return elements.toArray();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            var parent = (String) parentElement;
            List<String> children = new ArrayList<>();
            for (String name : parameterInfos.keySet()) {
                if (name != parent && name.startsWith(parent)
                        && name.substring(parent.length()).lastIndexOf("/") == 0) {
                    children.add(name);
                }
            }

            return children.toArray();
        }

        @Override
        public Object getParent(Object element) {

            var namespace = (String) element;

            var parent = namespace.substring(0, namespace.lastIndexOf("/"));
            if (parent.isEmpty()) {
                return null;

            }

            return parent;
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

    }

}
