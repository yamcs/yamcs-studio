package org.csstudio.opibuilder.widgets.editparts;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * A {@link TextCellEditor} that exposes a method to externally allow accepting a value.
 * 
 * This was introduced because we want to have the ability to accept a direct edit in SWT, when for example an action
 * button in Draw2D is clicked (both focus systems are independent, hence this trickery).
 */
public class CloseableTextCellEditor extends TextCellEditor {

    public CloseableTextCellEditor(Composite composite, int style) {
        super(composite, style);
    }

    /**
     * Accept the current value and close the direct editor
     */
    public void acceptValue() {
        super.focusLost();
    }
}
