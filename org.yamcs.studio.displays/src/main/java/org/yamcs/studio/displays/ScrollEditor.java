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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ScrollEditor extends EditorPart {

    public static final String ID = ScrollEditor.class.getName();
    private static final Logger log = Logger.getLogger(ScrollEditor.class.getName());

    ScrollViewer parameterTable;

    private FileEditorInput input;
    private ParameterTable fileInput;

    public static ScrollEditor createPVTableEditor() {
        var workbench = PlatformUI.getWorkbench();
        var window = workbench.getActiveWorkbenchWindow();
        var page = window.getActivePage();
        try {
            var input = new EmptyEditorInput();
            return (ScrollEditor) page.openEditor(input, ScrollEditor.ID);
        } catch (Exception e) {
            ExceptionDetailsErrorDialog.openError(page.getActivePart().getSite().getShell(),
                    "Cannot create Parameter Table", e);
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
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

            var gson = new Gson();
            var reader = new InputStreamReader(input.getFile().getContents());
            fileInput = gson.fromJson(reader, ParameterTable.class);
            if (fileInput == null) {
                fileInput = new ParameterTable();
            }

            for (ParameterInfo meta : YamcsPlugin.getMissionDatabase().getParameters()) {
                for (String parameter : fileInput.getParameters()) {
                    if (parameter.contains(meta.getQualifiedName())) {
                        info.add(meta);
                    }
                }
            }

            return info;
        } catch (JsonSyntaxException ex) {
            fileInput = new ParameterTable();
            return info;
        } catch (CoreException e) {
            log.log(Level.SEVERE, "Could not read parameter list", e);
            return null;
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        var fl = new FillLayout();
        fl.marginHeight = 0;
        fl.marginWidth = 0;
        parent.setLayout(fl);

        var sash = new SashForm(parent, SWT.VERTICAL);

        var tableWrapper = new Composite(sash, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));

        parameterTable = new ScrollViewer(tableWrapper);
        for (ParameterInfo info : loadData()) {
            parameterTable.addParameter(info);
        }

        parameterTable.refresh();
    }

    public ScrollEditor() {
        super();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        var input = getEditorInput();
        try {
            if (input.exists()) {
                var file = (IFile) input.getAdapter(IFile.class);
                var out = new ByteArrayOutputStream();
                saveToStream(monitor, parameterTable.getParameters(), out);
                loadData();
                file.setContents(new ByteArrayInputStream(out.toByteArray()), true, false, monitor);
            }
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
     */
    private void saveToStream(IProgressMonitor monitor, List<String> parameters, OutputStream stream) {
        if (monitor != null) {
            monitor.beginTask("Save", IProgressMonitor.UNKNOWN);
        }
        var out = new PrintWriter(stream);

        try {

            var gson = new Gson();

            fileInput.setParameters(parameters);
            gson.toJson(fileInput, out);

            firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (Exception e) {
            ExceptionDetailsErrorDialog.openError(getSite().getShell(), "Error while writing parameter list", e);
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
        return !(parameterTable.getParameters().size() == fileInput.getParameters().size()
                && parameterTable.getParameters().containsAll(fileInput.getParameters()));
    }

    @Override
    public boolean isSaveAsAllowed() {
        return isDirty();
    }

    @Override
    public void setFocus() {
        parameterTable.getTable().setFocus();
    }

    public ScrollViewer getParameterTable() {
        return parameterTable;
    }
}
