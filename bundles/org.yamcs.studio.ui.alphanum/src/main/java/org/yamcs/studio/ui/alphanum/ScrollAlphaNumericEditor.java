package org.yamcs.studio.ui.alphanum;


import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.ui.util.EmptyEditorInput;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.csstudio.utility.singlesource.ResourceHelper;
import org.csstudio.utility.singlesource.SingleSourcePlugin;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ScrollAlphaNumericEditor extends EditorPart {

    public static final String ID = ScrollAlphaNumericEditor.class.getName();
    private static final Logger log = Logger.getLogger(ScrollAlphaNumericEditor.class.getName());

    ScrollParameterTableViewer parameterTable;

    private FileEditorInput input;
    private AlphaNumericJson fileInput;


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
        List<ParameterInfo> info = new ArrayList<>();
        try {
            
            Gson gson = new Gson();   
            InputStreamReader reader = new InputStreamReader(input.getFile().getContents());
            fileInput = gson.fromJson(reader, AlphaNumericJson.class);
            if(fileInput == null) {
                fileInput = new AlphaNumericJson();
            }

            for (ParameterInfo meta :ParameterCatalogue.getInstance().getMetaParameters()) {
                for(String parameter : fileInput.getParameterList())
                    if(parameter.contains(meta.getQualifiedName()))
                        info.add(meta);
            }

            return info;
        }catch (JsonSyntaxException ex)  {
            fileInput = new AlphaNumericJson();
            return info;
        } catch (CoreException e) {
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
        final IEditorInput input = getEditorInput();
        final ResourceHelper resources = SingleSourcePlugin.getResourceHelper();
        try {
            saveToStream(monitor, parameterTable.getParameters(), resources.getOutputStream(input));
            loadData();
        } catch (Exception ex) {
            ExceptionDetailsErrorDialog.openError(getSite().getShell(), "Error while saving parameter list", ex);
            // Save failed, allow saving under a different name, or cancel
        }
    }
    

    /**
     * Save current model, mark editor as clean.
     *
     * @param monitor
     *            <code>IProgressMonitor</code>, may be <code>null</code>.
     * @param stream
     *            Output stream
     */
    private void saveToStream(final IProgressMonitor monitor, final List<String> parameters,
            final OutputStream stream) {
        if (monitor != null) {
            monitor.beginTask("Save", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        }
        final PrintWriter out = new PrintWriter(stream);

        try {

            Gson gson = new Gson();

            fileInput.setParameterList(parameters);
            gson.toJson(fileInput, out);

            firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (Exception ex) {
            ExceptionDetailsErrorDialog.openError(getSite().getShell(),"Error while writing parameter list", ex);
        } finally {
            out.close();
        }
        if (monitor != null) {
            monitor.done();
        } 
    }

    @Override
    public void doSaveAs() {

    }


    @Override
    public boolean isDirty() {       
        return !(parameterTable.getParameters().size() == fileInput.getParameterList().size()
                && parameterTable.getParameters().containsAll(fileInput.getParameterList()));
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
