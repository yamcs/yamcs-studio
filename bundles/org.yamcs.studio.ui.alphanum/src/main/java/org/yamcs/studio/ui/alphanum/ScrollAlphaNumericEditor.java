package org.yamcs.studio.ui.alphanum;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.ui.util.EmptyEditorInput;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
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

public class ScrollAlphaNumericEditor extends EditorPart {

    public static final String ID = ScrollAlphaNumericEditor.class.getName();
    private static final Logger log = Logger.getLogger(ScrollAlphaNumericEditor.class.getName());

    ScrollParameterTableViewer parameterTable;

    private FileEditorInput input;


    public static ScrollAlphaNumericEditor createPVTableEditor() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        final IWorkbenchPage page = window.getActivePage();
        try {
            final EmptyEditorInput input = new EmptyEditorInput(); //$NON-NLS-1$
            return (ScrollAlphaNumericEditor) page.openEditor(input, ScrollAlphaNumericEditor.ID);
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

        parameterTable = new ScrollParameterTableViewer(tableWrapper);
        for(ParameterInfo info : loadData())
            parameterTable.addParameter(info);

        parameterTable.refresh();   }

    public ScrollAlphaNumericEditor() {
        super();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        //		final IEditorInput input = getEditorInput();
        //		final ResourceHelper resources = SingleSourcePlugin.getResourceHelper();
        //		try {
        //			if (input.exists()) {
        //
        //				saveToStream(monitor, parameterTable.getParameters(), resources.getOutputStream(input));
        //			} else { // First save of Editor with empty input, prompt for name
        //				doSaveAs();
        //			}
        //		} catch (Exception ex) {
        //			ExceptionDetailsErrorDialog.openError(getSite().getShell(), "Error while saving parameter list", ex);
        //			// Save failed, allow saving under a different name, or cancel
        //			doSaveAs();
        //		}
    }

    //	/**
    //	 * Save current model, mark editor as clean.
    //	 *
    //	 * @param monitor
    //	 *            <code>IProgressMonitor</code>, may be <code>null</code>.
    //	 * @param stream
    //	 *            Output stream
    //	 */
    //	private void saveToStream(final IProgressMonitor monitor, final List<ParameterInfo> parameters,
    //			final OutputStream stream) {
    //		if (monitor != null) {
    //			monitor.beginTask("Save", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
    //		}
    //		final PrintWriter out = new PrintWriter(stream);
    //		try {
    //
    //			for(ParameterInfo info : parameters)
    //				out.println(info.getQualifiedName());
    //			ParameterContentProvider provider = (ParameterContentProvider)parameterTable.getContentProvider();
    //			provider.load(new ArrayList<>(provider.getParameter()));
    //			firePropertyChange(IEditorPart.PROP_DIRTY);
    //		} catch (Exception ex) {
    //			ExceptionDetailsErrorDialog.openError(getSite().getShell(),"Error while writing parameter list", ex);
    //		} finally {
    //			out.close();
    //		}
    //		if (monitor != null) {
    //			monitor.done();
    //		} 
    //	}

    @Override
    public void doSaveAs() {

    }


    @Override
    public boolean isDirty() {
        return false; // parameterTable.hasChanged();
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


    public ScrollParameterTableViewer getParameterTable() {
        return parameterTable;
    }


}
