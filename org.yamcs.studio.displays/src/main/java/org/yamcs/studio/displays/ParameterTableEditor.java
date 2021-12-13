/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

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
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.gson.Gson;

public class ParameterTableEditor extends EditorPart {

    public static final String ID = ParameterTableEditor.class.getName();
    private static final Logger log = Logger.getLogger(ParameterTableEditor.class.getName());

    private ParameterTableViewer tableViewer;

    private FileEditorInput input;
    private ParameterTable model;

    public static ParameterTableEditor createPVTableEditor() {
        var workbench = PlatformUI.getWorkbench();
        var window = workbench.getActiveWorkbenchWindow();
        var page = window.getActivePage();
        try {
            var input = new EmptyEditorInput();
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
        var fl = new FillLayout();
        fl.marginHeight = 0;
        fl.marginWidth = 0;
        parent.setLayout(fl);

        var sash = new SashForm(parent, SWT.VERTICAL);

        var tableWrapper = new Composite(sash, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));

        tableViewer = new ParameterTableViewer(tableWrapper);
        var provider = (ParameterTableContentProvider) tableViewer.getContentProvider();

        try {
            var gson = new Gson();
            var reader = new InputStreamReader(input.getFile().getContents());
            model = gson.fromJson(reader, ParameterTable.class);
            if (model == null) {
                model = new ParameterTable();
            }
            provider.setParameters(model.getParameters());

            for (var parameter : model.getParameters()) {
                var id = NamedObjectId.newBuilder().setName(parameter).build();
                var meta = YamcsPlugin.getMissionDatabase().getParameterInfo(id);
                if (meta != null) {
                    // tableViewer.attachParameterInfo(meta);
                }
            }
        } catch (CoreException e) {
            log.log(Level.SEVERE, "Could not read parameter list", e);
        }

        tableViewer.refresh();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        var input = getEditorInput();
        try {
            if (input.exists()) {
                var file = (IFile) input.getAdapter(IFile.class);
                var out = new ByteArrayOutputStream();
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

        try (var out = new PrintWriter(stream)) {
            var gson = new Gson();
            var provider = (ParameterTableContentProvider) tableViewer.getContentProvider();

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
