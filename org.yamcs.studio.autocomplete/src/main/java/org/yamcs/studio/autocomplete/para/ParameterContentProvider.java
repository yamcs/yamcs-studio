/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.autocomplete.para;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.studio.autocomplete.AutoCompleteHelper;
import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.IAutoCompleteProvider;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * PV Name lookup for Yamcs Parameters
 * <p>
 * AutoCompleteService will re-use one instance of this class for all lookups, calling <code>listResult</code> whenever
 * the user types a new character, using a new thread for each lookup. Before starting a new lookup, however,
 * <code>cancel()</code> is invoked. This means there are never multiple concurrent lookups started on purpose, but a
 * previously started lookup may still continue in its thread in case <code>cancel()</code> has no immediate effect.
 */
public class ParameterContentProvider implements IAutoCompleteProvider {

    @Override
    public boolean accept(ContentType type) {
        return type == ContentType.PVName;
    }

    public String getPrefix() {
        return ParameterContentParser.PARA_SOURCE;
    }

    public boolean requirePrefix() {
        return false;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        var content = desc.getValue();
        if (content.startsWith(getPrefix())) {
            content = content.substring(getPrefix().length());
        } else if (requirePrefix()) {
            return new AutoCompleteResult();
        }

        content = AutoCompleteHelper.trimWildcards(content);
        content = content.replaceAll("\\[[0-9]+\\]", "[]"); // Ignore specific index into array
        var namePattern = AutoCompleteHelper.convertToPattern(content);
        namePattern = Pattern.compile(namePattern.pattern(), Pattern.CASE_INSENSITIVE);

        var result = new AutoCompleteResult();
        var matchCount = 0;
        var mdb = YamcsPlugin.getMissionDatabase();
        if (mdb != null) {
            for (var para : mdb.getParameters()) {
                var pvCandidates = new ArrayList<String>();
                pvCandidates.add(para.getQualifiedName());
                if (para.hasType()) {
                    scanTypeForPvCandidates(para.getQualifiedName(), para.getType(), pvCandidates);
                }

                for (var pvCandidate : pvCandidates) {
                    var proposalValue = requirePrefix() ? getPrefix() + pvCandidate : pvCandidate;
                    var m = namePattern.matcher(proposalValue);
                    if (m.find()) {
                        var p = new Proposal(proposalValue, false);
                        p.addStyle(ProposalStyle.getDefault(m.start(), m.end() - 1));
                        result.addProposal(p);
                        matchCount++;
                        if (matchCount >= limit) {
                            break;
                        }
                    }
                }

                if (matchCount >= limit) {
                    break;
                }
            }
        }

        result.setCount(matchCount);
        return result;
    }

    private void scanTypeForPvCandidates(String basePvName, ParameterTypeInfo type, List<String> pvCandidates) {
        for (var member : type.getMemberList()) {
            var memberPvName = basePvName + "." + member.getName();
            pvCandidates.add(memberPvName);
            if (member.hasType()) {
                scanTypeForPvCandidates(memberPvName, member.getType(), pvCandidates);
            }
        }
        if (type.hasArrayInfo()) {
            var entryPvName = basePvName + "[]";
            var entryType = type.getArrayInfo().getType();
            scanTypeForPvCandidates(entryPvName, entryType, pvCandidates);
        }
    }

    @Override
    public synchronized void cancel() {
        // NOP, our search code should be fast enough to not cause problems
    }
}
