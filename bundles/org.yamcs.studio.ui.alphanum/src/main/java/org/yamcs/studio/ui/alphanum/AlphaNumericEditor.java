package org.yamcs.studio.ui.alphanum;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.ui.util.EmptyEditorInput;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.core.model.ParameterCatalogue;

public class AlphaNumericEditor extends EditorPart {

	public static final String ID = AlphaNumericEditor.class.getName();
	private static final Logger log = Logger.getLogger(AlphaNumericEditor.class.getName());

	ParameterTableViewer parameterTable;

	private FileEditorInput input;


	public static AlphaNumericEditor createPVTableEditor() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		final IWorkbenchPage page = window.getActivePage();
		try {
			final EmptyEditorInput input = new EmptyEditorInput(); //$NON-NLS-1$
			return (AlphaNumericEditor) page.openEditor(input, AlphaNumericEditor.ID);
		} catch (Exception ex) {
			ExceptionDetailsErrorDialog.openError(page.getActivePart().getSite().getShell(), "Cannot create PV Table", //$NON-NLS-1$
					ex);
		}
		return null;
	}

	@Override
	public void dispose() {
		super.dispose();
	}


	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		// "Site is incorrect" error results if the site is not set:
		this.input = (FileEditorInput) input;
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		setSite(site);

		this.getEditorSite().getActionBarContributor();

	}

	private List<ParameterInfo> loadData() {
		try {
			List<ParameterInfo> info = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input.getFile().getContents()));
			String line;
			while ((line = reader.readLine()) != null) {
				for (ParameterInfo meta :ParameterCatalogue.getInstance().getMetaParameters()) {
					if(line.contains(meta.getQualifiedName()))
						info.add(meta);
				}
			}

			return info;
		} catch (IOException | CoreException e) {
			log.log(Level.SEVERE, "Could not read parameter list", e);
			return null;
		}
	}


	@Override
	public void createPartControl(Composite parent) {
		FillLayout fl = new FillLayout();
		fl.marginHeight = 0;
		fl.marginWidth = 0;
		parent.setLayout(fl);

		SashForm sash = new SashForm(parent, SWT.VERTICAL);

		Composite tableWrapper = new Composite(sash, SWT.NONE);
		tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));

		parameterTable = new ParameterTableViewer(tableWrapper);
		ParameterContentProvider provider = (ParameterContentProvider)parameterTable.getContentProvider();
		provider.load(loadData());
		for(ParameterInfo info : loadData())
			parameterTable.addParameter(info);

		parameterTable.refresh();   }

	public AlphaNumericEditor() {
		super();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		final IEditorInput input = getEditorInput();
		try {
			if (input.exists()) {
			    IFile file = (IFile) input.getAdapter(IFile.class);
			    ByteArrayOutputStream out = new ByteArrayOutputStream();
			    saveToStream(parameterTable.getParameters(), out);
			    file.setContents(new ByteArrayInputStream(out.toByteArray()), true, false, monitor);
			} else { // First save of Editor with empty input, prompt for name
				doSaveAs();
			}
		} catch (Exception ex) {
			ExceptionDetailsErrorDialog.openError(getSite().getShell(), "Error while saving parameter list", ex);
			// Save failed, allow saving under a different name, or cancel
			doSaveAs();
		}
	}

	/**
	 * Save current model, mark editor as clean.
	 * 
	 * @param stream
	 *            Output stream
	 */
	private void saveToStream(List<ParameterInfo> parameters, OutputStream stream) {
		final PrintWriter out = new PrintWriter(stream);
		try {

			for(ParameterInfo info : parameters)
				out.println(info.getQualifiedName());
			ParameterContentProvider provider = (ParameterContentProvider)parameterTable.getContentProvider();
			provider.load(new ArrayList<>(provider.getParameter()));
			firePropertyChange(IEditorPart.PROP_DIRTY);
		} catch (Exception ex) {
			ExceptionDetailsErrorDialog.openError(getSite().getShell(),"Error while writing parameter list", ex);
		} finally {
			out.close();
		} 
	}

	@Override
	public void doSaveAs() {

	}


	@Override
	public boolean isDirty() {
		return parameterTable.hasChanged();
	}

	@Override
	public boolean isSaveAsAllowed() {
		if(isDirty())
			return true;
		return false;
	}

	@Override
	public void setFocus() {
		parameterTable.getTable().setFocus();

	}


	public ParameterTableViewer getParameterTable() {
		return parameterTable;
	}


}
