package org.csstudio.opibuilder.visualparts;

import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.yamcs.studio.autocomplete.ui.AutoCompleteTypes;
import org.yamcs.studio.autocomplete.ui.AutoCompleteWidget;
import org.yamcs.studio.autocomplete.ui.content.ContentProposalAdapter;
import org.yamcs.studio.autocomplete.ui.content.IContentProposalListener2;

/**
 * A text cell editor that allows pv name auto complete.
 */
public class PVNameTextCellEditor extends TextCellEditor {

    private ContentProposalAdapter contentProposalAdapter;
    private boolean proposalPopuped = false;

    public PVNameTextCellEditor(Composite parent) {
        super(parent);
        AutoCompleteWidget autoCompleteWidget = new AutoCompleteWidget(this, AutoCompleteTypes.Formula);
        autoCompleteWidget.getContentProposalAdapter().addContentProposalListener(
                new IContentProposalListener2() {

                    @Override
                    public void proposalPopupOpened(ContentProposalAdapter adapter) {
                        proposalPopuped = true;
                    }

                    @Override
                    public void proposalPopupClosed(ContentProposalAdapter adapter) {
                        proposalPopuped = false;
                    }
                });
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
        if (contentProposalAdapter != null) {
            contentProposalAdapter.addContentProposalListener(listener);
        }
    }
}
