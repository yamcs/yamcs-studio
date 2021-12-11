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

import org.csstudio.ui.util.wizards.WizardNewFileCreationPage;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Wizard page for the creation of new javascript files.
 */
public class NewJavaScriptWizardPage extends WizardNewFileCreationPage {

    public NewJavaScriptWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle("Create a new javascript");
        setDescription("Create a new javascript in the selected project or folder.");
    }

    @Override
    protected String getNewFileLabel() {
        return "Javascript File Name:";
    }

    @Override
    public String getFileExtension() {
        return "js";
    }
}
