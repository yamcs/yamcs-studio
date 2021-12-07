/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.autocomplete.ops;

import java.util.regex.Pattern;

import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
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
public class OpsContentProvider implements IAutoCompleteProvider {

    @Override
    public boolean accept(ContentType type) {
        return type == ContentType.PVName;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        var content = desc.getValue();
        if (content.startsWith(OpsContentParser.OPS_SOURCE)) {
            content = content.substring(OpsContentParser.OPS_SOURCE.length());
        }

        content = AutoCompleteHelper.trimWildcards(content);
        var namePattern = AutoCompleteHelper.convertToPattern(content);
        namePattern = Pattern.compile(namePattern.pattern(), Pattern.CASE_INSENSITIVE);

        var pvs = new AutoCompleteResult();
        var matchCount = 0;
        var mdb = YamcsPlugin.getMissionDatabase();
        if (mdb != null) {
            for (ParameterInfo para : mdb.getParameters()) {
                var opsname = findOpsname(para);
                if (opsname != null) {
                    var proposalValue = OpsContentParser.OPS_SOURCE + opsname;
                    var m = namePattern.matcher(proposalValue);
                    if (m.find()) {
                        var p = new Proposal(proposalValue, false);
                        p.addStyle(ProposalStyle.getDefault(m.start(), m.end() - 1));
                        pvs.addProposal(p);
                        matchCount++;
                        if (matchCount >= limit) {
                            break;
                        }
                    }
                }
            }
        }
        pvs.setCount(matchCount);
        return pvs;
    }

    private String findOpsname(ParameterInfo parameter) {
        for (NamedObjectId id : parameter.getAliasList()) {
            if (id.hasNamespace() && "MDB:OPS Name".equals(id.getNamespace())) {
                return id.getName();
            }
        }
        return null;
    }

    @Override
    public synchronized void cancel() {
        // NOP, our search code should be fast enough to not cause problems
    }
}
