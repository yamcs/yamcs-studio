/*******************************************************************************
* Copyright (c) 2010-2016 ITER Organization.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
******************************************************************************/
package org.csstudio.opibuilder.examples;

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
 * The import wizard of OPI Symbol Images Library.
 */
public class NewOPIImageLibExampleWizard extends Wizard implements IImportWizard {

    private static final String WINDOW_TITLE = "Install OPI Image Library";
    private static final String WIZARD_PAGE = "OPI Image Library";
    private static final String WIZARD_PAGE_TITLE = "Install OPI Image Library";
    private static final String WIZARD_PAGE_DESCRIPTION = "Creates a new project containing electrical and fluid symbols.";

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // NOP
    }

    @Override
    public void addPages() {
        super.addPages();
        setWindowTitle(WINDOW_TITLE);
        addPage(new WizardPage(WIZARD_PAGE) {

            @Override
            public void createControl(Composite parent) {
                setTitle(WIZARD_PAGE_TITLE);
                setDescription(WIZARD_PAGE_DESCRIPTION);
                Composite container = new Composite(parent, SWT.None);
                container.setLayout(new GridLayout());
                setControl(container);

                Label label = new Label(container, SWT.WRAP);
                GridData gd = new GridData();
                gd.widthHint = 500;
                label.setLayoutData(gd);

                label.setText("OPI Image Library will be imported to your workspace. "
                        + NLS.bind(
                                "If there is already a project named \"{0}\" in your workspace,"
                                        + "the import will fail. ",
                                InstallOPIImageLibraryAction.PROJECT_NAME)
                        + "Please rename or delete it and import again.");
            }
        });
    }

    @Override
    public boolean performFinish() {
        new InstallOPIImageLibraryAction().run(null);
        return true;
    }
}
