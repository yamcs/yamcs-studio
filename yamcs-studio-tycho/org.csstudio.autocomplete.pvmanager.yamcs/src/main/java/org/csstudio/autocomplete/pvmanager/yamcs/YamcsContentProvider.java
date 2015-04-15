package org.csstudio.autocomplete.pvmanager.yamcs;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.csstudio.autocomplete.AutoCompleteHelper;
import org.csstudio.autocomplete.AutoCompleteResult;
import org.csstudio.autocomplete.IAutoCompleteProvider;
import org.csstudio.autocomplete.parser.ContentDescriptor;
import org.csstudio.autocomplete.parser.ContentType;
import org.csstudio.autocomplete.proposals.Proposal;
import org.csstudio.autocomplete.proposals.ProposalStyle;
import org.csstudio.platform.libs.yamcs.MDBContextListener;
import org.csstudio.platform.libs.yamcs.YamcsPlugin;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

/**
 * PV Name lookup for Yamcs Parameters
 * <p>
 * AutoCompleteService will re-use one instance of this class for all lookups, calling
 * <code>listResult</code> whenever the user types a new character, using a new thread for each
 * lookup. Before starting a new lookup, however, <code>cancel()</code> is invoked. This means there
 * are never multiple concurrent lookups started on purpose, but a previously started lookup may
 * still continue in its thread in case <code>cancel()</code> has no immediate effect.
 * 
 * TODO this is marked in the osgi file as a high-level provider, we should try to make it as one of
 * the datasource-providers instead, but had some problems trying to figure that out, and this seems
 * to work fine for now.
 */
public class YamcsContentProvider implements IAutoCompleteProvider {

    private static final String YAMCS_SOURCE = "yamcs://";
    private static final Logger log = Logger.getLogger(YamcsContentProvider.class.getName());

    // Available parameters by their lowercase representation
    private ConcurrentHashMap<String, String> haystack = new ConcurrentHashMap<>();

    public YamcsContentProvider() {
        // Get initial list of parameters
        loadParameterNames(YamcsPlugin.getDefault().getParameterIds());

        // Subscribe to future updates
        YamcsPlugin.getDefault().addMdbListener(new MDBContextListener() {
            @Override
            public void onParametersChanged(List<NamedObjectId> parameterIds) {
                loadParameterNames(parameterIds);
            }
        });
    }

    /**
     * Builds an index of all available parameter names
     */
    private void loadParameterNames(List<NamedObjectId> parameterIds) {
        for (NamedObjectId id : parameterIds) {
            haystack.put(id.getName().toLowerCase(), id.getName());
        }
    }

    @Override
    public boolean accept(ContentType type) {
        return type == ContentType.PVName;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        String content = desc.getValue();
        if (content.startsWith(YAMCS_SOURCE)) {
            content = content.substring(YAMCS_SOURCE.length());
        }

        if (haystack == null) {
            log.info("Not matching pattern " + desc.getValue() + " since there's nothing to match with yet");
        }

        content = AutoCompleteHelper.trimWildcards(content);
        Pattern namePattern = AutoCompleteHelper.convertToPattern(content);
        namePattern = Pattern.compile(namePattern.pattern(), Pattern.CASE_INSENSITIVE);

        AutoCompleteResult pvs = new AutoCompleteResult();
        int matchCount = 0;
        for (Entry<String, String> hay : haystack.entrySet()) {
            Matcher m = namePattern.matcher(hay.getValue());
            if (m.find()) {
                Proposal p = new Proposal(hay.getValue(), false);
                p.addStyle(ProposalStyle.getDefault(m.start(), m.end() - 1));
                pvs.addProposal(p);
                matchCount++;
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
