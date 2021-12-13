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

import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.yamcs.protobuf.Mdb.ParameterInfo;

public class AddParameterWizard extends Wizard {

    AddParameterPage page;

    @Override
    public boolean performFinish() {
        if (page.getParameter() != null && !page.getParameter().isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public void addPages() {
        page = new AddParameterPage();
        addPage(page);
    }

    public List<ParameterInfo> getParameter() {
        return page.getParameter();
    }
}
