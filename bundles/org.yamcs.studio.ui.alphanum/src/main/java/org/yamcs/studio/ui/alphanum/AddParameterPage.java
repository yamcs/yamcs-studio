package org.yamcs.studio.ui.alphanum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.yamcs.studio.core.model.ParameterCatalogue;


public class AddParameterPage extends WizardPage {

	public static final String COL_NAMESPACE = "Namespace";
	public static final String COL_NAME = "Name";
	public static final int COLUMN_WIDTH = 10;
	public static final int COLUMN_MAX_WIDTH = 600;

	TreeViewer namespaceTable;
	TableViewer parameterTable;
	TableColumnLayout tcl;
	TreeColumnLayout trcl;
	List<ParameterInfo> selectedParameters;
	ParameterContentProvider contentProvider;

	Map<String, ArrayList<ParameterInfo>> parameterInfos;


	public AddParameterPage() {
		super("Choose parameters");
		setTitle("Choose Parameters");
		parameterInfos = new HashMap<>();
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.numColumns = 2;
		gl.makeColumnsEqualWidth = false;
		composite.setLayout(gl);

		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite tableWrapper1 = new Composite(composite, SWT.NONE);
		contentProvider = new ParameterContentProvider();
		trcl = new TreeColumnLayout();
		tableWrapper1.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableWrapper1.setLayout(trcl);

		namespaceTable = new TreeViewer(tableWrapper1, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		namespaceTable.getTree().setHeaderVisible(true);
		namespaceTable.getTree().setLinesVisible(true);

		// column container
		TreeViewerColumn pathColumn = new TreeViewerColumn(namespaceTable, SWT.NONE);
		pathColumn.getColumn().setText(COL_NAMESPACE);
		pathColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override 
			public String getText(Object element) {
				String namespace = (String) element;                       
				return namespace;
			}
		});
		trcl.setColumnData(pathColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));

		namespaceTable.addSelectionChangedListener(evt -> {
			IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
			if (sel.isEmpty()) {
				contentProvider.setNamespace(null);
				return;
			}
			contentProvider.setNamespace((String) sel.getFirstElement());


		});
		
		namespaceTable.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = new IStructuredSelection() {
					
					@Override
					public boolean isEmpty() {
						return false;
					}
					
					@Override
					public List toList() {
						return Arrays.asList(contentProvider.getElements(event));
					}
					
					@Override
					public Object[] toArray() {
						
						return contentProvider.getElements(event);
					}
					
					@Override
					public int size() {
						return contentProvider.getElements(event).length;
					}
					
					@Override
					public Iterator iterator() {
						return Arrays.asList(contentProvider.getElements(event)).iterator();
					}
					
					@Override
					public Object getFirstElement() {
						return null;
					}
				};
				parameterTable.setSelection(sel);
				
			}
		});

		
		namespaceTable.setContentProvider(new NamespaceContentProvider());
	
		
		namespaceTable.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object o1, Object o2) {
				String n1 = (String) o1;
				String n2 =	(String) o2;
				return n1.compareTo(n2);


			}
		});

		Composite tableWrapper2 = new Composite(composite, SWT.NONE);
		tcl = new TableColumnLayout();
		tableWrapper2.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableWrapper2.setLayout(tcl);
		
		parameterTable = new TableViewer(tableWrapper2, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		parameterTable.getTable().setHeaderVisible(true);
		parameterTable.getTable().setLinesVisible(true);

		
		TableViewerColumn nameColumn = new TableViewerColumn(parameterTable, SWT.NONE);
		nameColumn.getColumn().setText(COL_NAME);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ParameterInfo obj = (ParameterInfo) element;
				return obj.getQualifiedName();
			}
		});
		tcl.setColumnData(nameColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));

		parameterTable.addSelectionChangedListener(evt -> {
			IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
			if (sel.isEmpty()) {
				setParameter(new ArrayList<>());
				return;
			}
			List<ParameterInfo> parameters = new ArrayList<>();
			for(Object obj: sel.toArray()) {
				parameters.add((ParameterInfo) obj);
			}
			
			setParameter(parameters);
			setPageComplete(true);

		});

		parameterTable.setContentProvider(contentProvider);
		parameterTable.setInput(contentProvider);

		ParameterCatalogue.getInstance().getMetaParameters().forEach(pmtr -> {

			for (NamedObjectId alias : pmtr.getAliasList()) {
				String namespace = alias.getNamespace();
				if(!namespace.startsWith("/"))
					return;
				if(!parameterInfos.containsKey(namespace)) {
					parameterInfos.put(namespace, new ArrayList<>());
				}
				
				parameterInfos.get(namespace).add(pmtr);
				
				String parentns = namespace.substring(0, namespace.lastIndexOf("/"));
				while(!parentns.isEmpty()) {
					if(!parameterInfos.containsKey(parentns)) {
						parameterInfos.put(parentns, new ArrayList<>());
					}
					parentns = parentns.substring(0, parentns.lastIndexOf("/"));
					
				}
				
				
				
			}

		});
		namespaceTable.setInput(parameterInfos.keySet());
		
		parameterTable.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object o1, Object o2) {
				ParameterInfo n1 = (ParameterInfo) o1;
				ParameterInfo n2 =	(ParameterInfo) o2;
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
			if(namespace == null)
				return new String[0];
			
			return parameterInfos.get(namespace).toArray();
		}

		private void setNamespace(String namespace) {
			this.namespace = namespace;
			parameterTable.refresh();
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
			for(String name: parameterInfos.keySet()) {
				if(getParent(name) == null)
					elements.add(name);
			}			
			
			return elements.toArray();
		}
		
		@Override
		public Object[] getChildren(Object parentElement) {
			String parent = (String) parentElement;
			List<String> children = new ArrayList<>();
			for(String name: parameterInfos.keySet()) {
				if( name != parent && name.startsWith(parent) && name.substring(parent.length()).lastIndexOf("/") == 0) {
					children.add(name);
				}
			}
			
			return children.toArray();
		}
		
		@Override
		public Object getParent(Object element) {
			
			String namespace = (String) element;
			
			String parent = namespace.substring(0, namespace.lastIndexOf("/"));
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
