package org.yamcs.studio.css.core.pvmanager.autocomplete;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.csstudio.autocomplete.AutoCompleteHelper;
import org.csstudio.autocomplete.AutoCompleteResult;
import org.csstudio.autocomplete.IAutoCompleteProvider;
import org.csstudio.autocomplete.parser.ContentDescriptor;
import org.csstudio.autocomplete.parser.ContentType;
import org.csstudio.autocomplete.proposals.Proposal;
import org.csstudio.autocomplete.proposals.ProposalStyle;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.core.model.ParameterCatalogue;

/**
 * PV Name lookup for Yamcs Parameters
 * <p>
 * AutoCompleteService will re-use one instance of this class for all lookups, calling <code>listResult</code> whenever
 * the user types a new character, using a new thread for each lookup. Before starting a new lookup, however,
 * <code>cancel()</code> is invoked. This means there are never multiple concurrent lookups started on purpose, but a
 * previously started lookup may still continue in its thread in case <code>cancel()</code> has no immediate effect.
 */
public class XtceContentProvider implements IAutoCompleteProvider {

    @Override
    public boolean accept(ContentType type) {
        return type == ContentType.PVName;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        String content = desc.getValue();
        if (content.startsWith(XtceContentParser.XTCE_SOURCE)) {
            content = content.substring(XtceContentParser.XTCE_SOURCE.length());
        }

        content = AutoCompleteHelper.trimWildcards(content);
        Pattern namePattern = AutoCompleteHelper.convertToPattern(content);
        namePattern = Pattern.compile(namePattern.pattern(), Pattern.CASE_INSENSITIVE);

        AutoCompleteResult pvs = new AutoCompleteResult();
        int matchCount = 0;
        for (ParameterInfo para : ParameterCatalogue.getInstance().getMetaParameters()) {
            Matcher m = namePattern.matcher(para.getQualifiedName());
            if (m.find()) {
                Proposal p = new Proposal(para.getQualifiedName(), false);
                p.addStyle(ProposalStyle.getDefault(m.start(), m.end() - 1));
                pvs.addProposal(p);
                matchCount++;
                if (matchCount >= limit) {
                    break;
                }
            }
        }
        pvs.setCount(matchCount);
        return pvs;
    }

    @Override
    public synchronized void cancel() {
        // NOP, our search code should be fast enough to not cause problems
    }
}
