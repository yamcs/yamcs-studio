/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.displays.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.displays.AddParameterWizard;
import org.yamcs.studio.displays.ParameterTableViewer;

public class AddNewParameterAction extends Action {

    private ParameterTableViewer viewer;

    public AddNewParameterAction(ParameterTableViewer viewer) {
        this.viewer = viewer;
        setImageDescriptor(getImageDescriptor("icons/elcl16/add.png"));
    }

    @Override
    public void run() {
        var shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        var wizard = new AddParameterWizard();
        var dialog = new WizardDialog(shell, wizard);
        if (dialog.open() == Window.OK) {
            for (ParameterInfo info : wizard.getParameter()) {
                // viewer.attachParameterInfo(info);
            }
        }
    }

    private ImageDescriptor getImageDescriptor(String path) {
        return ImageDescriptor
                .createFromURL(FileLocator.find(Platform.getBundle("org.yamcs.studio.displays"), new Path(path), null));
    }
}
