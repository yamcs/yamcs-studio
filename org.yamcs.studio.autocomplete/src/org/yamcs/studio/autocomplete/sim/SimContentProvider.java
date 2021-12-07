/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.sim;

import java.util.regex.Pattern;

import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.IAutoCompleteProvider;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.FunctionDescriptor;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;
import org.yamcs.studio.autocomplete.tooltips.TooltipData;

/**
 * Simulation Data Source content provider. Provides all available functions & theirs tooltips.
 */
public class SimContentProvider implements IAutoCompleteProvider {

    @Override
    public boolean accept(ContentType type) {
        if (type == SimContentType.SimFunction) {
            return true;
        }
        return false;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        var result = new AutoCompleteResult();

        FunctionDescriptor functionDesc = null;
        if (desc instanceof FunctionDescriptor) {
            functionDesc = (FunctionDescriptor) desc;
        } else {
            return result; // empty result
        }

        var functionName = functionDesc.getFunctionName();
        var set = DSFunctionRegistry.getDefault().findFunctionSet(SimDSFunctionSet.name);

        // handle proposals
        var count = 0;
        if (!functionDesc.hasOpenBracket()) {
            var regex = functionName;
            regex = regex.replaceAll("\\*", ".*");
            regex = regex.replaceAll("\\?", ".");
            Pattern valuePattern = null;
            try {
                valuePattern = Pattern.compile("^" + regex); // start with !
            } catch (Exception e) {
                return result; // empty result
            }

            Proposal topProposal = null;
            DSFunction closestMatchingFunction = null;
            var offset = SimContentParser.SIM_SOURCE.length();
            for (DSFunction function : set.getFunctions()) {
                var m = valuePattern.matcher(function.getName());
                if (m.find()) {
                    var proposalStr = function.getName();
                    if (hasMandatoryArgument(function)) {
                        proposalStr += "(";
                    }
                    if (desc.getDefaultDataSource() != SimContentParser.SIM_SOURCE) {
                        proposalStr = SimContentParser.SIM_SOURCE + proposalStr;
                    }
                    var proposal = new Proposal(proposalStr, false);
                    var description = function.getDescription() + "\n\n" + generateSignature(function);
                    for (DSFunction poly : function.getPolymorphicFunctions()) {
                        description += "\n" + generateSignature(poly);
                    }
                    proposal.setDescription(description);
                    var currentArgIndex = -1;
                    if (hasMandatoryArgument(function)) {
                        currentArgIndex = 0;
                    }
                    proposal.addTooltipData(generateTooltipData(function, currentArgIndex));
                    for (DSFunction poly : function.getPolymorphicFunctions()) {
                        proposal.addTooltipData(generateTooltipData(poly, currentArgIndex));
                    }
                    proposal.addStyle(ProposalStyle.getDefault(0, offset + m.end() - 1));
                    proposal.setInsertionPos(desc.getStartIndex());
                    result.addProposal(proposal);
                    count++;
                    if (closestMatchingFunction == null
                            || closestMatchingFunction.getName().compareTo(function.getName()) > 0) {
                        closestMatchingFunction = function;
                        topProposal = proposal;
                    }
                }
            }
            // handle top proposals
            if (closestMatchingFunction != null && !functionName.isEmpty()) {
                result.addTopProposal(topProposal);
            }
        }
        result.setCount(count);

        // handle tooltip
        if (!functionDesc.isComplete()) {
            for (DSFunction function : set.findFunctions(functionName)) {
                // no tooltip for incomplete functions => use proposals
                if (function.getName().equals(functionName)) {
                    if (checkToken(function, functionDesc)) {
                        result.addTooltipData(generateTooltipData(function, functionDesc.getCurrentArgIndex()));
                    }
                    for (DSFunction poly : function.getPolymorphicFunctions()) {
                        if (checkToken(poly, functionDesc)) {
                            result.addTooltipData(generateTooltipData(poly, functionDesc.getCurrentArgIndex()));
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void cancel() {
    }

    private String generateSignature(DSFunction function) {
        var sb = new StringBuffer();
        sb.append("sim://" + function.getName() + "(");
        var nbArgs = function.getArgumentNames().size();
        for (var i = 0; i < nbArgs; i++) {
            sb.append("<");
            sb.append(function.getArgumentTypes().get(i).getSimpleName());
            sb.append(">");
            sb.append(function.getArgumentNames().get(i));
            if (i < nbArgs - 1) {
                sb.append(", ");
            }
        }
        if (function.isVarArgs()) {
            sb.append(",...");
        }
        sb.append(")");
        return sb.toString();
    }

    private boolean hasMandatoryArgument(DSFunction function) {
        if (function.getNbArgs() == 0) {
            return false;
        }
        for (DSFunction poly : function.getPolymorphicFunctions()) {
            if (poly.getNbArgs() == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean checkToken(DSFunction function, FunctionDescriptor token) {
        if (token.hasOpenBracket() && function.getNbArgs() == 0) {
            return false; // sim://noise( => no tooltip for sim://noise
        }
        if (token.hasOpenBracket() && function.getArgumentNames().size() < token.getArgs().size()
                && !function.isVarArgs()) {
            return false; // too much arguments
        }
        return true;
    }

    private TooltipData generateTooltipData(DSFunction function, int currentArgIndex) {
        // build content
        var td = new TooltipData();
        var sb = new StringBuilder();
        sb.append(function.getName());
        var nbArgs = function.getNbArgs();
        if (nbArgs > 0) {
            sb.append("(");
        }
        int from = 0, to = 0;
        for (var i = 0; i < nbArgs; i++) {
            if (i == currentArgIndex) {
                from = sb.length();
            }
            sb.append("<");
            sb.append(function.getArgumentTypes().get(i).getSimpleName());
            sb.append(">");
            sb.append(function.getArgumentNames().get(i));
            if (i == currentArgIndex) {
                to = sb.length();
            }
            if (i < nbArgs - 1) {
                sb.append(", ");
            }
        }
        if (function.isVarArgs()) {
            if (currentArgIndex >= nbArgs) {
                from = sb.length();
                to = from + 4;
            }
            sb.append(",...");
        }
        if (nbArgs > 0) {
            sb.append(")");
        }
        if (function.getTooltip() != null) {
            td.styles = new ProposalStyle[2];
            td.styles[0] = ProposalStyle.getDefault(from, to);
            from = sb.length() + 1;
            sb.append(" " + function.getTooltip());
            to = sb.length();
            td.styles[1] = ProposalStyle.getItalic(from, to);
        } else {
            td.styles = new ProposalStyle[1];
            td.styles[0] = ProposalStyle.getDefault(from, to);
        }
        td.value = sb.toString();

        return td;
    }

}
