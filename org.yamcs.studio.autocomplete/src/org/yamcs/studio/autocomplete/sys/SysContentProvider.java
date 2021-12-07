/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.sys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.IAutoCompleteProvider;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;
import org.yamcs.studio.autocomplete.proposals.TopProposalFinder;

/**
 * System Data Source content provider Provides all available system functions & system properties.
 */
public class SysContentProvider implements IAutoCompleteProvider {

    public static final String SYSTEM_FUNCTION = "system";
    public static final String SYSTEM_SEPARATOR = ".";

    @Override
    public boolean accept(ContentType type) {
        if (type == SysContentType.SysFunction) {
            return true;
        }
        return false;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        var result = new AutoCompleteResult();

        SysContentDescriptor sysDesc = null;
        if (desc instanceof SysContentDescriptor) {
            sysDesc = (SysContentDescriptor) desc;
        } else {
            return result; // empty result
        }

        var dotIndex = desc.getValue().indexOf(SYSTEM_SEPARATOR);
        if (dotIndex == -1) {
            result = provideFunctions(sysDesc, limit);
        } else if (desc.getValue().substring(0, dotIndex).equals(SYSTEM_FUNCTION)) {
            result = provideSystemProperties(sysDesc, limit);
        }
        Collections.sort(result.getProposals());

        return result;
    }

    private AutoCompleteResult provideFunctions(SysContentDescriptor sysDesc, int limit) {
        var result = new AutoCompleteResult();
        var count = 0;

        var regex = sysDesc.getValue();
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
        var offset = SysContentParser.SYS_SOURCE.length();
        for (String function : SysContentDescriptor.listFunctions()) {
            var m = valuePattern.matcher(function);
            if (m.find()) {
                var fctDisplay = function;
                if (sysDesc.getDefaultDataSource() != SysContentParser.SYS_SOURCE) {
                    fctDisplay = SysContentParser.SYS_SOURCE + function;
                }
                if (function.equals(SYSTEM_FUNCTION)) {
                    fctDisplay += SYSTEM_SEPARATOR;
                }
                var proposal = new Proposal(fctDisplay, false);
                proposal.setDescription(SysContentDescriptor.getDescription(function));
                proposal.addStyle(ProposalStyle.getDefault(0, offset + m.end() - 1));
                proposal.setInsertionPos(sysDesc.getStartIndex());
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

    private AutoCompleteResult provideSystemProperties(SysContentDescriptor sysDesc, int limit) {
        var result = new AutoCompleteResult();
        var count = 0;

        var dotIndex = sysDesc.getValue().indexOf(SYSTEM_SEPARATOR);
        var propValue = sysDesc.getValue().substring(dotIndex + 1);
        var regex = propValue.replaceAll("\\.", "\\\\.");
        ;
        regex = regex.replaceAll("\\*", ".*");
        regex = regex.replaceAll("\\?", ".");
        Pattern valuePattern = null;
        try {
            valuePattern = Pattern.compile("^" + regex); // start with !
        } catch (Exception e) {
            return result; // empty result
        }

        List<String> matchingProperties = new ArrayList<String>();
        var systemProperties = System.getProperties();
        Enumeration<?> enuProp = systemProperties.propertyNames();
        var offset = SysContentParser.SYS_SOURCE.length() + 7;
        while (enuProp.hasMoreElements()) {
            var propertyName = (String) enuProp.nextElement();
            var propertyValue = systemProperties.getProperty(propertyName);
            var m = valuePattern.matcher(propertyName);
            if (m.find()) {
                var propDisplay = SYSTEM_FUNCTION + SYSTEM_SEPARATOR + propertyName;
                if (sysDesc.getDefaultDataSource() != SysContentParser.SYS_SOURCE) {
                    propDisplay = SysContentParser.SYS_SOURCE + propDisplay;
                }
                var proposal = new Proposal(propDisplay, false);
                proposal.setDescription(propertyValue);
                proposal.addStyle(ProposalStyle.getDefault(0, offset + m.end() - 1));
                proposal.setInsertionPos(sysDesc.getStartIndex());
                if (count <= limit) {
                    result.addProposal(proposal);
                }
                matchingProperties.add(propertyName);
                count++;
            }
        }
        // handle top proposals
        var tpf = new TopProposalFinder(SYSTEM_SEPARATOR);
        for (Proposal tp : tpf.getTopProposals(propValue, matchingProperties)) {
            var propDisplay = SYSTEM_FUNCTION + SYSTEM_SEPARATOR + tp.getValue();
            if (sysDesc.getDefaultDataSource() != SysContentParser.SYS_SOURCE) {
                propDisplay = SysContentParser.SYS_SOURCE + propDisplay;
            }
            var proposal = new Proposal(propDisplay, tp.isPartial());
            var propertyValue = systemProperties.getProperty(tp.getValue());
            proposal.setDescription(propertyValue);
            var tpStyle = tp.getStyles().get(0);
            proposal.addStyle(ProposalStyle.getDefault(tpStyle.from, (offset + tpStyle.to)));
            proposal.setInsertionPos(sysDesc.getStartIndex());
            result.addTopProposal(proposal);
        }

        result.setCount(count);
        return result;
    }

    @Override
    public void cancel() {
    }

}
