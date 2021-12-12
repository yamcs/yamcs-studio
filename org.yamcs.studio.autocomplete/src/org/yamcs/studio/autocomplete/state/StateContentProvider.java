/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.autocomplete.state;

import java.util.Collections;
import java.util.regex.Pattern;

import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.IAutoCompleteProvider;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;

public class StateContentProvider implements IAutoCompleteProvider {

    @Override
    public boolean accept(ContentType type) {
        return type == StateContentType.StateFunction;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        var result = new AutoCompleteResult();

        StateContentDescriptor stateDesc = null;
        if (desc instanceof StateContentDescriptor) {
            stateDesc = (StateContentDescriptor) desc;
        } else {
            return result; // empty result
        }

        result = provideFunctions(stateDesc, limit);
        Collections.sort(result.getProposals());

        return result;
    }

    private AutoCompleteResult provideFunctions(StateContentDescriptor stateDesc, int limit) {
        var result = new AutoCompleteResult();
        var count = 0;

        var regex = stateDesc.getValue();
        regex = regex.replaceAll("\\*", ".*");
        regex = regex.replaceAll("\\?", ".");
        Pattern valuePattern = null;
        try {
            valuePattern = Pattern.compile("^" + regex); // start with !
        } catch (Exception e) {
            return result; // empty result
        }

        Proposal topProposal = null;
        String closestMatchingFunction = null;
        var offset = StateContentParser.STATE_SOURCE.length();
        for (String function : StateContentDescriptor.listFunctions()) {
            var m = valuePattern.matcher(function);
            if (m.find()) {
                var fctDisplay = function;
                fctDisplay = StateContentParser.STATE_SOURCE + function;
                var proposal = new Proposal(fctDisplay, false);
                proposal.setDescription(StateContentDescriptor.getDescription(function));
                proposal.addStyle(ProposalStyle.getDefault(0, offset + m.end() - 1));
                proposal.setInsertionPos(stateDesc.getStartIndex());
                if (count <= limit) {
                    result.addProposal(proposal);
                }
                count++;
                if (closestMatchingFunction == null || closestMatchingFunction.compareTo(function) > 0) {
                    closestMatchingFunction = function;
                    topProposal = proposal;
                }
            }
        }
        // handle top proposals
        if (closestMatchingFunction != null) {
            result.addTopProposal(topProposal);
        }

        result.setCount(count);
        return result;
    }

    @Override
    public void cancel() {
    }
}
