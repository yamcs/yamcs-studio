/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.examples;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * The import wizard to import OPI Examples.
 */
public class ImportOPIExamplesWizard extends Wizard implements IImportWizard {

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // NOP
    }

    @Override
    public void addPages() {
        super.addPages();
        setWindowTitle("Install OPI Examples");
        addPage(new WizardPage("OPI Examples") {

            @Override
            public void createControl(Composite parent) {
                setTitle("Install OPI Examples");
                setDescription("Creates a new project with sample OPI displays");
                var container = new Composite(parent, SWT.None);
                container.setLayout(new GridLayout());
                setControl(container);

                var label = new Label(container, SWT.WRAP);
                var gd = new GridData();
                gd.widthHint = 500;
                label.setLayoutData(gd);

                label.setText("OPI Examples will be imported to your workspace. "
                        + NLS.bind("If there is already a project named \"{0}\" in your workspace,"
                                + "the import will fail. ", InstallOPIExamplesAction.PROJECT_NAME)
                        + "Please rename or delete it and import again.");
            }
        });
    }

    @Override
    public boolean performFinish() {
        new InstallOPIExamplesAction().run(null);
        return true;
    }
}
