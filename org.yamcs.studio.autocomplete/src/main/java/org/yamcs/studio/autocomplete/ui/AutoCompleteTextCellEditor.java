/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui;

import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.yamcs.studio.autocomplete.ui.content.ContentProposalAdapter;
import org.yamcs.studio.autocomplete.ui.content.IContentProposalListener2;
import org.yamcs.studio.autocomplete.ui.history.AutoCompleteHistory;

public class AutoCompleteTextCellEditor extends TextCellEditor {

    private ContentProposalAdapter contentProposalAdapter;
    private boolean popupOpen = false; // true, if popup is currently open

    public AutoCompleteTextCellEditor(Composite parent, String type) {
        super(parent);

        var provider = new AutoCompleteProposalProvider(type);
        contentProposalAdapter = new ContentProposalAdapter(text, new TextContentAdapter(), provider,
                AutoCompleteWidget.getActivationKeystroke(), AutoCompleteWidget.getAutoactivationChars());
        enableContentProposal(provider, AutoCompleteWidget.getActivationKeystroke(),
                AutoCompleteWidget.getAutoactivationChars());
    }

    public AutoCompleteTextCellEditor(Composite parent, String type, List<Control> historyHandlers) {
        this(parent, type);
        if (historyHandlers != null) {
            for (var handler : historyHandlers) {
                getHistory().installListener(handler);
            }
        }
    }

    private void enableContentProposal(AutoCompleteProposalProvider provider, KeyStroke keyStroke,
            char[] autoActivationCharacters) {
        // Listen for popup open/close events to be able to handle focus events
        // correctly
        contentProposalAdapter.addContentProposalListener(new IContentProposalListener2() {
            @Override
            public void proposalPopupClosed(ContentProposalAdapter adapter) {
                popupOpen = false;
            }

            @Override
            public void proposalPopupOpened(ContentProposalAdapter adapter) {
                popupOpen = true;
            }
        });
    }

    /**
     * Return the {@link ContentProposalAdapter} of this cell editor.
     *
     * @return the {@link ContentProposalAdapter}
     */
    public ContentProposalAdapter getContentProposalAdapter() {
        return contentProposalAdapter;
    }

    @Override
    protected void focusLost() {
        if (!popupOpen) {
            // Focus lost deactivates the cell editor.
            // This must not happen if focus lost was caused by activating
            // the completion proposal popup.
            super.focusLost();
        }
    }

    @Override
    protected boolean dependsOnExternalFocusListener() {
        // Always return false;
        // Otherwise, the ColumnViewerEditor will install an additional focus
        // listener
        // that cancels cell editing on focus lost, even if focus gets lost due
        // to
        // activation of the completion proposal popup. See also bug 58777.
        return false;
    }

    public AutoCompleteHistory getHistory() {
        return contentProposalAdapter.getHistory();
    }

    @Override
    protected void fireApplyEditorValue() {
        if (getValue() != null) {
            getHistory().addEntry(getValue().toString());
        }
        getContentProposalAdapter().getHelper().close(false);
        super.fireApplyEditorValue();
    }
}
