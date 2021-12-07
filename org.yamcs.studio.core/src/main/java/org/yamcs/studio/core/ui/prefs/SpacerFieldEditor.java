/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.prefs;

import org.eclipse.swt.widgets.Composite;

/**
 * A field editor for adding space to a preference page. Implemented as an empty label field editor.
 */
public class SpacerFieldEditor extends LabelFieldEditor {

    public SpacerFieldEditor(Composite parent) {
        super("", parent);
    }
}
