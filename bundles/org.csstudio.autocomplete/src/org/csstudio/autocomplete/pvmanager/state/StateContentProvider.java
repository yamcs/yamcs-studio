package org.csstudio.autocomplete.pvmanager.state;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.csstudio.autocomplete.AutoCompleteResult;
import org.csstudio.autocomplete.IAutoCompleteProvider;
import org.csstudio.autocomplete.parser.ContentDescriptor;
import org.csstudio.autocomplete.parser.ContentType;
import org.csstudio.autocomplete.proposals.Proposal;
import org.csstudio.autocomplete.proposals.ProposalStyle;

public class StateContentProvider implements IAutoCompleteProvider {

    @Override
    public boolean accept(ContentType type) {
        return type == StateContentType.StateFunction;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        AutoCompleteResult result = new AutoCompleteResult();

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
        AutoCompleteResult result = new AutoCompleteResult();
        int count = 0;

        String regex = stateDesc.getValue();
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
        int offset = StateContentParser.STATE_SOURCE.length();
        for (String function : StateContentDescriptor.listFunctions()) {
            Matcher m = valuePattern.matcher(function);
            if (m.find()) {
                String fctDisplay = function;
                if (stateDesc.getDefaultDataSource() != StateContentParser.STATE_SOURCE) {
                    fctDisplay = StateContentParser.STATE_SOURCE + function;
                }
                Proposal proposal = new Proposal(fctDisplay, false);
                proposal.setDescription(StateContentDescriptor.getDescription(function));
                proposal.addStyle(ProposalStyle.getDefault(0, offset + m.end() - 1));
                proposal.setInsertionPos(stateDesc.getStartIndex());
                if (count <= limit) {
                    result.addProposal(proposal);
                }
                count++;
                if (closestMatchingFunction == null
                        || closestMatchingFunction.compareTo(function) > 0) {
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
