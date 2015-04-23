package org.yamcs.studio.client.ui.prefs;

import org.eclipse.swt.widgets.Composite;

/**
 * A field editor for adding space to a preference page.
 * Implemented as an empty label field editor.
 */
public class SpacerFieldEditor extends LabelFieldEditor {

    public SpacerFieldEditor(Composite parent) {
        super("", parent);
    }
}
