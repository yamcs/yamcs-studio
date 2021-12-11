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

import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.yamcs.studio.editor.base.Activator;

public class SchemaDecorator implements ILightweightLabelDecorator {

    public static final String ID = "org.csstudio.opibuilder.SchemaDecorator";

    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof IFile) {
            var file = (IFile) element;
            if ("opi".equalsIgnoreCase(file.getFileExtension())) {
                var schemaPath = PreferencesHelper.getSchemaOPIPath();
                if (schemaPath != null) {
                    var schemaFile = ResourceUtil.getIFileFromIPath(schemaPath);
                    if (schemaFile.equals(file)) {
                        var descriptor = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
                                "icons/ovr16/annotation_tsk.png");
                        decoration.addOverlay(descriptor, IDecoration.TOP_RIGHT);
                        decoration.addSuffix(" [schema]");
                    }
                }
            }
        }
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }
}
