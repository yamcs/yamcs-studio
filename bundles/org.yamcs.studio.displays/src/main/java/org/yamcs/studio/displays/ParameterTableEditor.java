
package org.yamcs.studio.displays;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.model.ParameterCatalogue;

import com.google.gson.Gson;

public class ParameterTableEditor extends EditorPart {

    public static final String ID = ParameterTableEditor.class.getName();
    private static final Logger log = Logger.getLogger(ParameterTableEditor.class.getName());

    private ParameterTableViewer tableViewer;

    private FileEditorInput input;
    private ParameterTable model;

    public static ParameterTableEditor createPVTableEditor() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        try {
            EmptyEditorInput input = new EmptyEditorInput();
            return (ParameterTableEditor) page.openEditor(input, ParameterTableEditor.ID);
        } catch (Exception e) {
            ExceptionDetailsErrorDialog.openError(page.getActivePart().getSite().getShell(),
                    "Cannot create Parameter Table", e);
        }
        return null;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        this.input = (FileEditorInput) input;
        setSite(site);
        setInput(input);
        setPartName(input.getName());
        setSite(site);
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

        tableViewer = new ParameterTableViewer(tableWrapper);
        ParameterTableContentProvider provider = (ParameterTableContentProvider) tableViewer.getContentProvider();

        try {
            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(input.getFile().getContents());
            model = gson.fromJson(reader, ParameterTable.class);
            if (model == null) {
                model = new ParameterTable();
            }
            provider.setParameters(model.getParameters());

            for (String parameter : model.getParameters()) {
                NamedObjectId id = NamedObjectId.newBuilder().setName(parameter).build();
                ParameterInfo meta = ParameterCatalogue.getInstance().getParameterInfo(id);
                if (meta != null) {
                    tableViewer.attachParameterInfo(meta);
                }
            }
        } catch (CoreException e) {
            log.log(Level.SEVERE, "Could not read parameter list", e);
        }

        tableViewer.refresh();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        IEditorInput input = getEditorInput();
        try {
            if (input.exists()) {
                IFile file = (IFile) input.getAdapter(IFile.class);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                saveToStream(monitor, tableViewer.getParameters(), out);
                file.setContents(new ByteArrayInputStream(out.toByteArray()), true, false, monitor);
            } else { // First save of Editor with empty input, prompt for name
                doSaveAs();
            }
        } catch (Exception e) {
            ExceptionDetailsErrorDialog.openError(getSite().getShell(), "Error while saving parameter list", e);
            // Save failed, allow saving under a different name, or cancel
            doSaveAs();
        }
    }

    /**
     * Save current model, mark editor as clean.
     */
    private void saveToStream(IProgressMonitor monitor, List<String> parameters, OutputStream stream) {
        if (monitor != null) {
            monitor.beginTask("Save", IProgressMonitor.UNKNOWN);
        }

        try (PrintWriter out = new PrintWriter(stream)) {
            Gson gson = new Gson();
            ParameterTableContentProvider provider = (ParameterTableContentProvider) tableViewer.getContentProvider();

            model.setParameters(parameters);
            gson.toJson(model, out);

            provider.setParameters(new ArrayList<>(provider.getParameters()));
            firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (Exception e) {
            ExceptionDetailsErrorDialog.openError(getSite().getShell(), "Error while writing parameter list", e);
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
        return tableViewer.hasChanged();
    }

    @Override
    public boolean isSaveAsAllowed() {
        return isDirty();
    }

    @Override
    public void setFocus() {
        tableViewer.getTable().setFocus();
    }
}
