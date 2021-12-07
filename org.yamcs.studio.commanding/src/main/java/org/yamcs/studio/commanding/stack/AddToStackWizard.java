/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

public class AddToStackWizard extends Wizard {

    private StackedCommand command;
    private AddToStackWizardPage2 page2;

    @Override
    public String getWindowTitle() {
        return "Add Command";
    }

    @Override
    public void addPages() {
        command = new StackedCommand();
        addPage(new AddToStackWizardPage1(command));
        page2 = new AddToStackWizardPage2(command);
        addPage(page2);
    }

    @Override
    public boolean performFinish() {
        page2.getAssignments().forEach((argumentInfo, value) -> {
            command.addAssignment(argumentInfo, value);
        });
        return true;
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        // NOP. Page2 is dynamic, and needs to be created *after* page1
    }

    public StackedCommand getTelecommand() {
        return command;
    }

    @Override
    public boolean canFinish() {
        var page2Control = page2.getControl();
        return page2Control != null && page2Control.isVisible() && page2.isPageComplete();
    }
}
