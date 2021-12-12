/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.loc;

import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.IAutoCompleteProvider;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;
import org.yamcs.studio.autocomplete.tooltips.TooltipData;

/**
 * Local Data Source content provider. Provides all available VType & content syntax assistance.
 */
public class LocalContentProvider implements IAutoCompleteProvider {

    @Override
    public boolean accept(ContentType type) {
        return type == LocalContentType.LocalPV;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        var result = new AutoCompleteResult();

        LocalContentDescriptor locDesc = null;
        if (desc instanceof LocalContentDescriptor) {
            locDesc = (LocalContentDescriptor) desc;
        } else {
            return result; // empty result
        }

        if (locDesc.isComplete()) {
            return result; // empty result
        }

        // handle proposals
        var count = 0;
        if (locDesc.isCompletingVType() && locDesc.getvType() != null) {
            var type = locDesc.getvType();
            Proposal topProposal = null;
            String closestMatchingType = null;
            for (String vType : LocalContentDescriptor.listVTypes()) {
                if (vType.startsWith(type)) {
                    var prefix = locDesc.getPvName() + LocalContentParser.VTYPE_START;
                    prefix = LocalContentParser.LOCAL_SOURCE + prefix;
                    var offset = prefix.length();
                    var proposal = new Proposal(prefix + vType + LocalContentParser.VTYPE_END, false);
                    proposal.setDescription(LocalContentDescriptor.getVTypeDescription(vType));
                    proposal.addStyle(ProposalStyle.getDefault(0, offset + type.length() - 1));
                    proposal.setInsertionPos(desc.getStartIndex());
                    result.addProposal(proposal);
                    count++;
                    if (closestMatchingType == null || closestMatchingType.compareTo(vType) > 0) {
                        closestMatchingType = vType;
                        topProposal = proposal;
                    }
                }
            }
            // handle top proposals
            if (closestMatchingType != null && !type.isEmpty()) {
                result.addTopProposal(topProposal);
            }
        }
        result.setCount(count);

        // handle tooltip
        TooltipData td = null;
        if (locDesc.isCompletingInitialValue()) {
            td = new TooltipData();
            td.value = "pvname";
            var vType = locDesc.getvType();
            if (vType != null) {
                td.value += LocalContentParser.VTYPE_START + locDesc.getvType() + LocalContentParser.VTYPE_END;
            }
            td.value += LocalContentParser.INITIAL_VALUE_START;
            var start = td.value.length();
            td.value += locDesc.getInitialValueTooltip();
            var end = td.value.length();
            td.value += LocalContentParser.INITIAL_VALUE_END;
            td.styles = new ProposalStyle[1];
            if (locDesc.checkParameters()) {
                td.styles[0] = ProposalStyle.getDefault(start, end);
            } else {
                td.styles[0] = ProposalStyle.getError(start, end);
            }
            result.addTooltipData(td);

        } else if (locDesc.isCompletingVType()) {
            td = new TooltipData();
            td.value = "pvname<type>";
            td.styles = new ProposalStyle[1];
            td.styles[0] = ProposalStyle.getDefault(6, 12);
            result.addTooltipData(td);

            td = new TooltipData();
            td.value = "pvname<type>(initialValue)";
            td.styles = new ProposalStyle[1];
            td.styles[0] = ProposalStyle.getDefault(6, 12);
            result.addTooltipData(td);

        } else {
            int from = 6, to = 12; // bold <type>
            if (locDesc.getvType() == null) { // bold pvname
                from = 0;
                to = 6;
                td = new TooltipData();
                td.value = "pvname";
                td.styles = new ProposalStyle[1];
                td.styles[0] = ProposalStyle.getDefault(from, to);
                result.addTooltipData(td);
            }

            td = new TooltipData();
            td.value = "pvname<type>";
            td.styles = new ProposalStyle[1];
            td.styles[0] = ProposalStyle.getDefault(from, to);
            result.addTooltipData(td);

            td = new TooltipData();
            td.value = "pvname<type>(initialValue)";
            td.styles = new ProposalStyle[1];
            td.styles[0] = ProposalStyle.getDefault(from, to);
            result.addTooltipData(td);
        }

        return result;
    }

    @Override
    public void cancel() {
    }

}
