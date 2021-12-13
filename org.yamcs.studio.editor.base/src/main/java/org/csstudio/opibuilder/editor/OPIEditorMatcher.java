/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.editor;

import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;

public class OPIEditorMatcher implements IEditorMatchingStrategy {

    @Override
    public boolean matches(IEditorReference editorRef, IEditorInput input) {
        try {
            var editorInput = editorRef.getEditorInput();
            var editorInputPath = ResourceUtil.getPathInEditor(editorInput);
            var inputPath = ResourceUtil.getPathInEditor(input);
            return editorInputPath.equals(inputPath);
        } catch (PartInitException e) {
            return false;
        }
    }
}
