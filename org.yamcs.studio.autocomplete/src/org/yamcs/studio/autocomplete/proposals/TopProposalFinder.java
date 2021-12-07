/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.proposals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.yamcs.studio.autocomplete.AutoCompleteHelper;

/**
 * Helper that uses delimiters to find the common matching token in a provided list using delimiters.
 */
public class TopProposalFinder {

    private String delimiters;

    public TopProposalFinder(String delimiters) {
        this.delimiters = delimiters;
    }

    public List<Proposal> getTopProposals(String name, Collection<String> proposals) {
        Set<Proposal> set = new LinkedHashSet<Proposal>();

        if (delimiters == null || delimiters.isEmpty()) {
            return new ArrayList<Proposal>(set); // empty result
        }

        var cleanedName = AutoCompleteHelper.trimWildcards(name);
        var namePattern = AutoCompleteHelper.convertToPattern(cleanedName);
        Proposal topProposal = null;
        for (String proposal : proposals) {
            var m = namePattern.matcher(proposal);
            if (m.find()) {
                var start = m.end();
                if (start == proposal.length()) {
                    topProposal = new Proposal(proposal, false);
                } else {
                    topProposal = findToken(proposal, start);
                }
                if (topProposal != null) {
                    topProposal.addStyle(ProposalStyle.getDefault(m.start(), m.end() - 1));
                    set.add(topProposal);
                }
            }
        }
        return new ArrayList<Proposal>(set);
    }

    private Proposal findToken(String s, int fromIndex) {
        if (fromIndex < 0 || fromIndex >= s.length()) {
            return null;
        }
        var sub = s.substring(fromIndex);
        var st = new StringTokenizer(sub, delimiters, true);
        if (st.hasMoreTokens()) {
            var token = st.nextToken();
            var endIndex = fromIndex + token.length();
            if (endIndex == s.length()) {
                var value = s.substring(0, endIndex);
                if (value != null && !value.isEmpty()) {
                    return new Proposal(value, false);
                }
            }
            var hasDelimiter = false;
            for (char d : delimiters.toCharArray()) {
                if (token.indexOf(d) >= 0) {
                    hasDelimiter = true;
                }
            }
            if (!hasDelimiter) {
                endIndex++;
            }
            var value = s.substring(0, endIndex);
            if (value != null && !value.isEmpty()) {
                return new Proposal(value, true);
            }
        }
        return null;
    }
}
