/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Empty IEditorInput.
 *
 * <p>
 * When an editor is started without a file, for example from the main menu, this can be used as its initial input.
 */
public class EmptyEditorInput implements IEditorInput {
    final private ImageDescriptor icon;

    /**
     * Initialize
     *
     * @param icon
     *            Desired icon
     */
    public EmptyEditorInput(ImageDescriptor icon) {
        this.icon = icon;
    }

    /** Initialize */
    public EmptyEditorInput() {
        this(null);
    }

    /** Cause application title to reflect the 'not saved' state. */
    @Override
    public String getName() {
        return "<Not saved to file>";
    }

    /** Cause tool top to reflect the 'not saved' state. */
    @Override
    public String getToolTipText() {
        return "This configuration has not been saved to a file";
    }

    /** @return Returns <code>false</code> since no file exists. */
    @Override
    public boolean exists() {
        return false;
    }

    /** Returns no image. */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return icon;
    }

    /** Can't persist. */
    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    /** Don't adapt. */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }
}
