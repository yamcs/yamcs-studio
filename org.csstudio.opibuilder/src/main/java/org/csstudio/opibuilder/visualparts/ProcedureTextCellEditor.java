/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.yamcs.client.base.ResponseObserver;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.spell.api.ListProceduresRequest;
import org.yamcs.studio.spell.api.ListProceduresResponse;
import org.yamcs.studio.spell.api.ProcedureInfo;

/**
 * A text cell editor that allows procedure autocomplete.
 */
public class ProcedureTextCellEditor extends TextCellEditor {

    private ContentProposalAdapter contentProposalAdapter;

    public ProcedureTextCellEditor(Composite parent) {
        super(parent);

        var knownProcedures = new CopyOnWriteArrayList<ProcedureInfo>();

        // Attempt to fetch available procedures.
        var spellClient = YamcsPlugin.getSpellClient();
        if (spellClient != null) {
            var request = ListProceduresRequest.newBuilder()
                    .setInstance(YamcsPlugin.getInstance())
                    .build();
            var f = new CompletableFuture<ListProceduresResponse>();
            spellClient.listProcedures(null, request, new ResponseObserver<>(f));
            f.thenAccept(response -> {
                knownProcedures.addAll(response.getProceduresList());
            });
        }

        addBulbDecorator(getControl(), "Procedure path. If connected, type for completion");

        var provider = (IContentProposalProvider) (contents, position) -> {
            var resultList = new ArrayList<IContentProposal>();
            var pattern = createProposalPattern(contents);
            for (var procedure : knownProcedures) {
                if (pattern != null && !pattern.matcher(procedure.getId()).matches()) {
                    continue;
                }
                resultList.add(new ContentProposal(procedure.getId()));
            }
            return resultList.toArray(new IContentProposal[resultList.size()]);
        };

        contentProposalAdapter = new ContentProposalAdapter(
                getControl(),
                new TextContentAdapter(),
                provider,
                null, null);
        contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
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

    /**
     * Creates a simple {@link Pattern} that can be used for matching content assist proposals. The pattern ignores
     * leading blanks and allows '*' as a wildcard matching multiple arbitrary characters.
     *
     * @param content
     *            to create the pattern from
     * @return the pattern, or {@code null} if none could be created
     */
    private Pattern createProposalPattern(String content) {
        // Make the simplest possible pattern check: allow "*"
        // for multiple characters.
        var patternString = content;
        // Ignore spaces in the beginning.
        while (patternString.length() > 0 && patternString.charAt(0) == ' ') {
            patternString = patternString.substring(1);
        }

        // We quote the string as it may contain spaces
        // and other stuff colliding with the pattern.
        patternString = Pattern.quote(patternString);

        patternString = patternString.replaceAll("\\x2A", ".*");

        // Make sure we add a (logical) * at the beginning and end.
        if (!patternString.startsWith(".*")) {
            patternString = ".*" + patternString;
        }
        if (!patternString.endsWith(".*")) {
            patternString = patternString + ".*";
        }

        try {
            return Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }

    private ControlDecoration addBulbDecorator(Control control, String tooltip) {
        var dec = new ControlDecoration(control, SWT.TOP | SWT.LEFT);

        dec.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(
                FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());

        dec.setShowOnlyOnFocus(true);
        dec.setShowHover(true);

        dec.setDescriptionText(tooltip);
        return dec;
    }
}
