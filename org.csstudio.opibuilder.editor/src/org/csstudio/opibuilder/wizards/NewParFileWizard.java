/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.wizards;

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

public class NewParFileWizard extends Wizard implements INewWizard {

    private NewParFileWizardPage parFilePage;

    private IStructuredSelection selection;

    private IWorkbench workbench;

    @Override
    public void addPages() {
        parFilePage = new NewParFileWizardPage("PARFilePage", selection);
        addPage(parFilePage);
    }

    @Override
    public boolean performFinish() {
        var file = parFilePage.createNewFile();

        if (file == null) {
            return false;
        }

        try {
            workbench.getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file),
                    "org.yamcs.studio.displays.ParamterTableEditor");
        } catch (PartInitException e) {
            MessageDialog.openError(null, "Open Par File error",
                    "Failed to open the newly created Parameter Table. \n" + e.getMessage());
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "ParEditor activation error", e);
        }

        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }
}
