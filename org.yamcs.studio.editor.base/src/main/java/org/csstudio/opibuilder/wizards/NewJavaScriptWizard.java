/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.wizards;

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * A wizard for creating new Javascript File.
 */
public class NewJavaScriptWizard extends Wizard implements INewWizard {

    private NewJavaScriptWizardPage jsFilePage;

    private IStructuredSelection selection;

    private IWorkbench workbench;

    @Override
    public void addPages() {
        jsFilePage = new NewJavaScriptWizardPage("JavaScriptFilePage", selection);
        addPage(jsFilePage);
    }

    @Override
    public boolean performFinish() {
        var file = jsFilePage.createNewFile();

        if (file == null) {
            return false;
        }

        var dw = workbench.getActiveWorkbenchWindow();
        try {
            if (dw != null) {
                var page = dw.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
        } catch (PartInitException e) {
            MessageDialog.openError(null, "Open JavaScript File error",
                    "Failed to open the newly created JavaScript File. \n" + e.getMessage());
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "JS Editor error", e);
        }

        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }
}
