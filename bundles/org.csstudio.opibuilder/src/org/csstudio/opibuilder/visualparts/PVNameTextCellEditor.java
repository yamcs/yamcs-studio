package org.csstudio.opibuilder.visualparts;

import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * A text cell editor that allows pv name auto complete.
 */
public class PVNameTextCellEditor extends TextCellEditor {

    private boolean proposalPopuped = false;

    public PVNameTextCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected boolean dependsOnExternalFocusListener() {
        // focus listener is controlled here.
        return false;
    }

    @Override
    protected void focusLost() {
        if (!proposalPopuped) {
            super.focusLost();
        }
    }

    public void applyValue() {
        fireApplyEditorValue();
    }

    /**
     * Add a listener that will be executed when pv name is seleteced by double click on proposal dialog.
     */
    public void addContentProposalListener(IContentProposalListener listener) {
    }
}
