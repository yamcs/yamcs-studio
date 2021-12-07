/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import java.io.InputStream;

import org.csstudio.opibuilder.util.MacrosInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;

public interface IRunnerInput extends IPathEditorInput, IPersistableElement {

    /**
     * @param displayOpenManager
     *            the displayOpenManager to set
     */
    public void setDisplayOpenManager(DisplayOpenManager displayOpenManager);

    /**
     * @return the displayOpenManager
     */
    public DisplayOpenManager getDisplayOpenManager();

    /**
     * @return the macrosInput
     */
    public MacrosInput getMacrosInput();

    public InputStream getInputStream() throws Exception;

}
