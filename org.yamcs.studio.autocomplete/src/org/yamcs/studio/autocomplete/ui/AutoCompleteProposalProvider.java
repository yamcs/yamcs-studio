/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui;

import java.util.ArrayList;
import java.util.List;

import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.AutoCompleteService;
import org.yamcs.studio.autocomplete.AutoCompleteType;
import org.yamcs.studio.autocomplete.IAutoCompleteResultListener;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.ui.content.ContentProposalList;
import org.yamcs.studio.autocomplete.ui.content.IContentProposalSearchHandler;

/**
 * Implements {@link IAutoCompleteProposalProvider} and manages the {@link ContentProposalList} with results from
 * {@link AutoCompleteService}.
 */
public class AutoCompleteProposalProvider implements IAutoCompleteProposalProvider {

    private final String type;
    private ContentProposalList currentList;
    private Long currentId;

    public AutoCompleteProposalProvider(String type) {
        this.type = type;
        this.currentList = new ContentProposalList();
    }

    @Override
    public void getProposals(String contents, IContentProposalSearchHandler handler) {
        currentId = System.currentTimeMillis();
        synchronized (currentList) {
            currentList.clear();
            currentList.setOriginalValue(contents);
        }
        var cns = AutoCompleteService.getInstance();
        var expected = cns.get(currentId, AutoCompleteType.valueOf(type), contents, new IAutoCompleteResultListener() {

            @Override
            public void handleResult(Long uniqueId, Integer index, AutoCompleteResult result) {
                if (uniqueId == currentId) {
                    synchronized (currentList) {
                        currentList.responseReceived();
                    }
                    if (result == null) {
                        return;
                    }

                    List<Proposal> contentProposals = new ArrayList<Proposal>();
                    if (result.getProposals() != null) {
                        for (Proposal proposal : result.getProposals()) {
                            contentProposals.add(proposal);
                        }
                    }
                    var contentProposalsArray = contentProposals.toArray(new Proposal[contentProposals.size()]);

                    List<Proposal> topContentProposals = new ArrayList<Proposal>();
                    if (result.getTopProposals() != null) {
                        for (Proposal proposal : result.getTopProposals()) {
                            topContentProposals.add(proposal);
                        }
                    }

                    ContentProposalList cpl = null;
                    synchronized (currentList) {
                        if (result.getProvider() != null) {
                            currentList.addProposals(result.getProvider(), contentProposalsArray, result.getCount(),
                                    index);
                        }
                        currentList.addTopProposals(topContentProposals);
                        cpl = currentList.clone();
                    }
                    handler.handleResult(cpl);
                    handler.handleTooltips(result.getTooltips());
                    // System.out.println("PROCESSED: " + uniqueId + ", " + index);
                }
            }
        });
        currentList.setExpected(expected);
    }

    @Override
    public boolean hasProviders() {
        return AutoCompleteService.getInstance().hasProviders(type);
    }

    @Override
    public void cancel() {
        AutoCompleteService.getInstance().cancel(type);
    }

    @Override
    public String getType() {
        return type;
    }
}
