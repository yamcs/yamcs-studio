/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui.history;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.yamcs.studio.autocomplete.AutoCompleteHelper;
import org.yamcs.studio.autocomplete.AutoCompletePlugin;
import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.IAutoCompleteProvider;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.preferences.Preferences;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;
import org.yamcs.studio.autocomplete.proposals.TopProposalFinder;
import org.yamcs.studio.autocomplete.ui.AutoCompleteTypes;

/**
 * History proposal provider. Retrieves matching proposals from history. Always handles the full auto-completed content
 * by using {@link ContentDescriptor} originalValue and ignore wildcards.
 */
public class AutoCompleteHistoryProvider implements IAutoCompleteProvider {

    public static final String NAME = "History";

    @Override
    public boolean accept(ContentType type) {
        return true;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        var content = desc.getOriginalContent();
        var startIndex = 0;
        if (desc.getContentType().equals(ContentType.PVName)) {
            content = desc.getValue();
            startIndex = desc.getStartIndex();
        }
        var result = new AutoCompleteResult();
        var cleanedName = AutoCompleteHelper.trimWildcards(content);
        var namePattern = AutoCompleteHelper.convertToPattern(cleanedName);
        if (namePattern == null) {
            return result;
        }

        var entryType = AutoCompleteTypes.PV;
        if (content.startsWith("=")) {
            entryType = AutoCompleteTypes.Formula;
        }

        var fifo = new LinkedList<String>();
        fifo.addAll(AutoCompletePlugin.getDefault().getHistory(entryType));
        if (fifo.isEmpty()) {
            return result; // Empty result
        }

        var count = 0;
        for (var entry : fifo) {
            var m = namePattern.matcher(entry);
            if (m.find()) {
                if (count < limit) {
                    var proposal = new Proposal(entry, false);
                    proposal.addStyle(ProposalStyle.getDefault(m.start(), m.end() - 1));
                    proposal.setInsertionPos(startIndex);
                    result.addProposal(proposal);
                }
                count++;
            }
        }
        result.setCount(count);

        var trf = new TopProposalFinder(Preferences.getSeparators());
        for (var p : trf.getTopProposals(Pattern.quote(cleanedName), fifo)) {
            result.addTopProposal(p);
        }

        return result;
    }

    @Override
    public void cancel() {
    }
}
