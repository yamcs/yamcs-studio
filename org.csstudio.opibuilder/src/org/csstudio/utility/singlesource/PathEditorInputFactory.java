/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.singlesource;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * Factory for restoring a persisted {@link PathEditorInput}
 * 
 * @author Kay Kasemir
 */
public class PathEditorInputFactory implements IElementFactory {

    public static final String ID = "org.csstudio.utility.singlesource.PathEditorInputFactory";

    public static final String TAG_PATH = "path";

    /** {@inheritDoc} */
    @Override
    public IAdaptable createElement(final IMemento memento) {
        String port_path = memento.getString(TAG_PATH);
        if (port_path == null) {
            return null;
        }

        // TODO Use URL Path
        IPath path = Path.fromPortableString(port_path);
        return new PathEditorInput(path);
    }
}
