/********************************************************************************
 * Copyright (c) 2014 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;

/**
 * {@link IEditorLauncher} that opens display in new OPIShell.
 *
 * <p>
 * Registered in plugin.xml as "Editor" for *.opi files, allowing users to launch displays from the Eclipse Navigator,
 * or by opening the file in the "default editor" based on the Eclipse registry.
 */
public class ShellLauncher implements IEditorLauncher {
    @Override
    public void open(final IPath path) {
        // The path is an absolute system path, which needs to be transformed to workspace path
        IPath workspacePath = LauncherHelper.systemPathToWorkspacePath(path);
        OPIShell.openOPIShell(workspacePath, null);

    }
}
