package org.yamcs.studio.ui.alphanum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.yamcs.protobuf.Mdb.ContainerInfo;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Mdb.SequenceEntryInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.model.ContainerCatalogue;


public class AddParameterPage extends WizardPage {

	//TODO make it so it only shows parameters when container is selected
	//TODO Fix search/implement filter
	//TODO send to table
	
	
    public static final String COL_CONTAINER = "Container";
    public static final String COL_NAME = "Name";
    public static final int COLUMN_WIDTH = 10;
    public static final int COLUMN_MAX_WIDTH = 600;
	
    TreeViewer containerTreeTable;
    TreeColumnLayout tcl;
    ParameterInfo selectedParameter;

    List<String> namespaces = new ArrayList<>();
    
	
	public AddParameterPage() {
		super("Choose parameters");
		setTitle("Choose Parameters");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        composite.setLayout(gl);

        // add filter box
        Text searchbox = new Text(composite, SWT.SEARCH | SWT.BORDER | SWT.ICON_CANCEL);
        searchbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite tableWrapper = new Composite(composite, SWT.NONE);
        tcl = new TreeColumnLayout();
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableWrapper.setLayout(tcl);

        containerTreeTable = new TreeViewer(tableWrapper, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        containerTreeTable.getTree().setHeaderVisible(true);
        containerTreeTable.getTree().setLinesVisible(true);

        // column container
        TreeViewerColumn pathColumn = new TreeViewerColumn(containerTreeTable, SWT.NONE);
        pathColumn.getColumn().setText(COL_CONTAINER);
        pathColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override 
            public String getText(Object element) {
            	if(element instanceof ParameterInfo) {
            		return "";
            	}
                ContainerInfo cnt = (ContainerInfo) element;
                          
                return cnt.getQualifiedName();
            }
        });
        tcl.setColumnData(pathColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));

        // column  name
        TreeViewerColumn nameColumn = new TreeViewerColumn(containerTreeTable, SWT.NONE);
        nameColumn.getColumn().setText(COL_NAME);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
            	if(element instanceof ParameterInfo) {
            		ParameterInfo obj = (ParameterInfo) element;
            		return obj.getQualifiedName();
            	}
                return "";
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));
// TODO
//        // on item selection update significance message and page completion status
        containerTreeTable.addSelectionChangedListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.isEmpty()) {
                setParameter(null);
                return;
            }
            
            if(sel.getFirstElement() instanceof ParameterInfo) {
            	setParameter((ParameterInfo) sel.getFirstElement());
            	setPageComplete(true);
            } else {
            	setParameter(null);
            	return;
            }
           
        });
        ContainerTreeContentProvider commandTreeContentProvider = new ContainerTreeContentProvider();
        containerTreeTable.setContentProvider(commandTreeContentProvider);

        // load command list
        Collection<ContainerInfo> containerInfos = new ArrayList<>();
        //TODO
        ContainerCatalogue.getInstance().getMetaContainers().forEach(ctn -> {

            // add aliases columns
            for (NamedObjectId alias : ctn.getAliasList()) {
                String namespace = alias.getNamespace();
                if (!namespaces.contains(namespace) && !namespace.startsWith("/")) {
                    namespaces.add(namespace);
                    addAliasColumn(namespace);
                }
            }
            containerInfos.add(ctn);
        });
        containerTreeTable.setInput(containerInfos);
        containerTreeTable.expandAll();

        // adjust columns width to content up to COLUMN_MAX_WIDTH
        // with a small hack to display full data on the first column
        for (TreeColumn tc : containerTreeTable.getTree().getColumns())
            tc.pack();
        pathColumn.getColumn().setWidth(pathColumn.getColumn().getWidth() + 11 * commandTreeContentProvider.nbLevels);
        for (TreeColumn tc : containerTreeTable.getTree().getColumns()) {
            if (tc.getWidth() > COLUMN_MAX_WIDTH)
                tc.setWidth(COLUMN_MAX_WIDTH);
        }

        // filter
//        CommandInfoTreeViewerFilter filter = new CommandInfoTreeViewerFilter(commandTreeContentProvider);
//        commandsTreeTable.addFilter(filter);
//        searchbox.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyReleased(KeyEvent ke) {
//                filter.setSearchTerm(searchbox.getText());
//                containerTreeTable.refresh();
//                containerTreeTable.expandAll();
//            }
//        });

        containerTreeTable.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
            	if(o1 instanceof ParameterInfo) {
            		if(o2 instanceof ParameterInfo) {
            			ParameterInfo n1 = (ParameterInfo) o1;
            			ParameterInfo n2 =	(ParameterInfo) o2;
            			return n1.getQualifiedName().compareTo(n2.getQualifiedName());
            		}
            		return -1;
            	} 
            	if (o2 instanceof ParameterInfo)
            		return 1;
                ContainerInfo c1 = (ContainerInfo) o1;
                ContainerInfo c2 = (ContainerInfo) o2;
                
                return c1.getQualifiedName().compareTo(c2.getQualifiedName());
            }
        });


	}
	
    private void setParameter(ParameterInfo element) {
    	selectedParameter = element;
	}
    
    public ParameterInfo getParameter() {
    	return selectedParameter;
    }

	// Add dynamically columns for each alias of a command
    private void addAliasColumn(String namespace) {

        TreeViewerColumn aliasColumn = new TreeViewerColumn(containerTreeTable, SWT.NONE);
        aliasColumn.getColumn().setText(namespace);

        aliasColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ContainerInfo ctn = (ContainerInfo) element;
                List<NamedObjectId> aliases = ctn.getAliasList();
                for (NamedObjectId aliase : aliases) {
                    if (aliase.getNamespace().equals(namespace))
                        return aliase.getName();
                }
                return "";
            }
        });
        tcl.setColumnData(aliasColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));
    }
	
	
	
	 private class ContainerTreeContentProvider implements ITreeContentProvider {

	        ArrayList<ContainerInfo> containerInfos;
	        public int nbLevels = 1;

	        @Override
	        public void dispose() {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	        }

	        @SuppressWarnings("unchecked")
	        @Override
	        public Object[] getElements(Object inputElement) {
	            containerInfos = (ArrayList<ContainerInfo>) inputElement;
	            ArrayList<ContainerInfo> rootCommands = new ArrayList<>();

	            // find root commands
	            for (ContainerInfo ci : containerInfos) {
	            	
	                if (!hasParent(ci))
	                    rootCommands.add(ci);
	            }

	            // compute number of inheritance level
	            int currentNbLevels = 1;
	            for (ContainerInfo ci : containerInfos) {
	                currentNbLevels = nbParents(ci) + 1;
	                if (currentNbLevels > nbLevels)
	                    nbLevels = currentNbLevels;
	            }

	            return rootCommands.toArray();
	        }

	        private boolean hasParent(ContainerInfo ci) {
	            return ci.getBaseContainer() != null && !ci.getBaseContainer().getName().equals("");
	        }

	        private int nbParents(ContainerInfo ci) {
	            if (hasParent(ci)) {
	            	ContainerInfo parent = (ContainerInfo) getParent(ci);
	                return nbParents(parent) + 1;
	            } else {
	                return 0;
	            }
	        }

	        @Override
	        public Object[] getChildren(Object parentElement) {
	        	if( parentElement instanceof ParameterInfo) {
	        		return new Object[0];
	        	}
	            ArrayList<Object> children = new ArrayList<>();
	            ContainerInfo parentCi = (ContainerInfo) parentElement;
	            for (ContainerInfo ci : containerInfos) {
	                if (ci.getBaseContainer().getQualifiedName().equals(parentCi.getQualifiedName()))
	                    children.add(ci);
	            }
	            
                for (SequenceEntryInfo entry: parentCi.getEntryList()) {
                	if(entry.hasParameter())
                		children.add(entry.getParameter());
                }
	            
	            return children.toArray();
	        }

	        @Override
	        public Object getParent(Object element) {
	        	ContainerInfo baseCommand = ((ContainerInfo) element).getBaseContainer();
	            for (ContainerInfo ci : containerInfos) {
	                if (ci.getQualifiedName().equals(baseCommand.getQualifiedName()))
	                    return ci;
	            }
	            return null;
	        }

	        @Override
	        public boolean hasChildren(Object element) {
	            Object[] children = getChildren(element);
	            return children != null && children.length > 0;
	        }

	    }


}
