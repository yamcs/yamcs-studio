/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.IAutoCompleteProvider;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.FunctionDescriptor;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;
import org.yamcs.studio.autocomplete.tooltips.TooltipData;
import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.formula.FormulaRegistry;

/**
 * PV formula functions provider.
 *
 */
public class FormulaFunctionProvider implements IAutoCompleteProvider {

    private Map<String, List<FormulaFunction>> functions;

    public FormulaFunctionProvider() {
        functions = new TreeMap<String, List<FormulaFunction>>();
        for (var setName : FormulaRegistry.getDefault().listFunctionSets()) {
            var set = FormulaRegistry.getDefault().findFunctionSet(setName);
            for (var function : set.getFunctions()) {
                var functionList = functions.get(function.getName());
                if (functionList == null) {
                    functionList = new ArrayList<FormulaFunction>();
                    functions.put(function.getName(), functionList);
                }
                functionList.add(function);
            }
        }
    }

    @Override
    public boolean accept(ContentType type) {
        if (type == ContentType.FormulaFunction) {
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
        var nameToFind = functionDesc.getFunctionName();

        // handle proposals
        var count = 0;
        // insertionPos is not yet provided for formula
        // TODO: improve parser
        var originalContent = desc.getOriginalContent();
        var insertionPos = originalContent.lastIndexOf(nameToFind);
        if (!functionDesc.hasOpenBracket()) {
            Proposal topProposal = null;
            String closestMatchingFunction = null;
            for (var functionName : functions.keySet()) {
                if (functionName.startsWith(nameToFind)) {
                    var proposal = new Proposal(functionName + "(", false);

                    var description = functions.get(functionName).get(0).getDescription() + "\n\n";
                    for (var ff : functions.get(functionName)) {
                        description += generateSignature(ff);
                    }
                    proposal.setDescription(description);
                    for (var ff : functions.get(functionName)) {
                        proposal.addTooltipData(generateTooltipData(ff, 0));
                    }

                    proposal.addStyle(ProposalStyle.getDefault(0, nameToFind.length() - 1));
                    proposal.setInsertionPos(insertionPos);
                    proposal.setFunction(true); // display function icon
                    result.addProposal(proposal);
                    count++;
                    if (closestMatchingFunction == null || closestMatchingFunction.compareTo(functionName) > 0) {
                        closestMatchingFunction = functionName;
                        topProposal = proposal;
                    }
                }
            }
            // handle top proposals
            if (closestMatchingFunction != null) {
                result.addTopProposal(topProposal);
            }
        }
        result.setCount(count);

        // handle tooltip
        if (functionDesc.hasOpenBracket() && !functionDesc.isComplete()) {
            for (var setName : FormulaRegistry.getDefault().listFunctionSets()) {
                var set = FormulaRegistry.getDefault().findFunctionSet(setName);
                for (var function : set.findFunctions(nameToFind)) {
                    if (function.getName().equals(nameToFind)) {
                        if (function.getArgumentNames().size() >= functionDesc.getArgs().size()
                                || function.isVarArgs()) {
                            result.addTooltipData(generateTooltipData(function, functionDesc.getCurrentArgIndex()));
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

    private String generateSignature(FormulaFunction function) {
        var sb = new StringBuffer();
        if (function.getReturnType() != null) {
            sb.append("<");
            sb.append(function.getReturnType().getSimpleName());
            sb.append("> ");
        }
        sb.append(function.getName() + "(");
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

    private TooltipData generateTooltipData(FormulaFunction function, int currentArgIndex) {
        var td = new TooltipData();
        var sb = new StringBuilder();
        sb.append(function.getName() + "(");
        var nbArgs = function.getArgumentNames().size();
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
        sb.append(")");
        td.styles = new ProposalStyle[1];
        td.styles[0] = ProposalStyle.getDefault(from, to);
        td.value = sb.toString();
        return td;
    }
}
