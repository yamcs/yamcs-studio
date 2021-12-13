/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.examples;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportYSSLandingWizard extends Wizard implements IImportWizard {

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // NOP
    }

    @Override
    public void addPages() {
        super.addPages();
        setWindowTitle("Install YSS Landing");
        addPage(new WizardPage("YSS Landing") {
            @Override
            public void createControl(Composite parent) {
                setTitle("Install YSS Landing");
                var container = new Composite(parent, SWT.NONE);
                container.setLayout(new GridLayout());
                setControl(container);

                var label = new Label(container, SWT.WRAP);
                var gd = new GridData();
                gd.widthHint = 500;
                label.setLayoutData(gd);

                label.setText("The 'YSS Landing' project will be imported to your workspace. This project "
                        + "is useful for demo purposes and runs against the simulation example included "
                        + "in the Yamcs repository.\n\n" + "If there is already a project named \""
                        + InstallYSSLandingAction.PROJECT_NAME + "\" in your workspace, the import will fail. "
                        + "Please rename or delete it and import again.");
            }
        });
    }

    @Override
    public boolean performFinish() {
        new InstallYSSLandingAction().run(null);
        return true;
    }
}
