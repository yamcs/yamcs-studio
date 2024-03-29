/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.yamcs.studio.autocomplete.preferences.Preferences;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;
import org.yamcs.studio.autocomplete.ui.IAutoCompleteProposalProvider;

/**
 * Handles all proposals from all providers. Applies rules on top proposals. Filled by
 * {@link IAutoCompleteProposalProvider}.
 */
public class ContentProposalList {

    private String originalValue;
    private List<Proposal> topProposalList;

    // provider => proposals
    private Map<String, Proposal[]> proposalMap;

    // provider => count
    private Map<String, Integer> countMap;

    // index => provider
    private SortedMap<Integer, String> providerMap;

    private int length = 0;
    private int expected = 0;
    private int responded = 0;

    private final int maxTopProposals;
    private boolean hasContentMatchingProposal = false;

    public ContentProposalList() {
        topProposalList = new ArrayList<>();
        proposalMap = new HashMap<>();
        countMap = new HashMap<>();
        providerMap = new TreeMap<>();
        maxTopProposals = Preferences.getMaxTopResults();
    }

    public ContentProposalList(ContentProposalList list) {
        originalValue = list.originalValue;
        topProposalList = new ArrayList<>(list.topProposalList);
        proposalMap = new HashMap<>(list.proposalMap);
        countMap = new HashMap<>(list.countMap);
        providerMap = new TreeMap<>(list.providerMap);
        length = list.length;
        expected = list.expected;
        responded = list.responded;
        maxTopProposals = list.maxTopProposals;
        hasContentMatchingProposal = list.hasContentMatchingProposal;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    private boolean startWithContent(Proposal proposal) {
        var insertionPos = proposal.getInsertionPos();
        if (insertionPos >= originalValue.length()) {
            return false;
        }
        if (proposal.getValue().startsWith(originalValue.substring(insertionPos))) {
            return true;
        }
        return false;
    }

    public void addTopProposals(List<Proposal> proposals) {
        if (proposals == null || proposals.isEmpty()) {
            return;
        }
        if (maxTopProposals == 0 && proposals.size() == 1) {
            if (!proposals.get(0).getValue().equals(originalValue)) {
                var index = topProposalList.indexOf(proposals.get(0));
                if (index == -1) {
                    proposals.get(0).setStartWithContent(startWithContent(proposals.get(0)));
                    proposals.get(0).setOriginalValue(originalValue);
                    topProposalList.add(proposals.get(0));
                } else {
                    topProposalList.get(index).increment();
                }
            }
        } else if (maxTopProposals > 0) {
            for (var proposal : proposals) {
                if (!proposal.getValue().equals(originalValue)) {
                    var index = topProposalList.indexOf(proposal);
                    if (index == -1) {
                        proposal.setStartWithContent(startWithContent(proposal));
                        proposal.setOriginalValue(originalValue);
                        topProposalList.add(proposal);
                    } else {
                        topProposalList.get(index).increment();
                    }
                }
            }
        }
        Collections.sort(topProposalList);
    }

    public List<Proposal> getTopProposalList() {
        List<Proposal> list = new ArrayList<>();
        if (topProposalList.size() == 0) {
            return list;
        }

        var originalTopProposal = new Proposal(originalValue, hasContentMatchingProposal ? false : true);
        originalTopProposal.addStyle(ProposalStyle.getDefault(0, originalValue.length()));

        if (topProposalList.size() == 1 && !originalValue.contains("*")
                && topProposalList.get(0).getStartWithContent()) {
            list.add(topProposalList.get(0));
            list.add(originalTopProposal);
        } else {
            list.add(originalTopProposal);
            var index = 0;
            for (var tp : topProposalList) {
                if (maxTopProposals == 0) {
                    list.add(tp);
                } else if (index <= maxTopProposals - 1) {
                    list.add(tp);
                }
                index++;
            }
        }

        // We do not display top proposals if the content match a proposal and all
        // provided top proposals match a complete proposal
        var allComplete = true;
        for (var tp : list) {
            if (tp.isPartial()) {
                allComplete = false;
            }
        }
        if (allComplete) {
            list.clear();
        }

        return list;
    }

    public void addProposals(String provider, Proposal[] proposals, Integer count, Integer index) {
        for (var p : proposals) {
            p.setOriginalValue(originalValue);
            if (p.getValue().equals(originalValue)) {
                hasContentMatchingProposal = true;
            }
        }
        proposalMap.put(provider, proposals);
        countMap.put(provider, count);
        length += proposals.length;
        providerMap.put(index, provider);
    }

    public Proposal[] getProposals(String provider) {
        return proposalMap.get(provider);
    }

    public Integer getCount(String provider) {
        return countMap.get(provider);
    }

    public List<String> getProviderList() {
        List<String> list = new ArrayList<>();
        for (var provider : providerMap.values()) {
            if (provider != null && !provider.isEmpty()) {
                list.add(provider);
            }
        }
        return list;
    }

    public int length() {
        return length;
    }

    public int fullLength() {
        return length() + getTopProposalList().size();
    }

    public void clear() {
        topProposalList.clear();
        proposalMap.clear();
        countMap.clear();
        providerMap.clear();
        length = 0;
        expected = 0;
        responded = 0;
        hasContentMatchingProposal = false;
    }

    @Override
    public ContentProposalList clone() {
        return new ContentProposalList(this);
    }

    @Override
    public String toString() {
        return "ContentProposalList [originalValue=" + originalValue + ", topProposalList=" + topProposalList
                + ", proposalMap=" + proposalMap + ", countMap=" + countMap + ", providerMap=" + providerMap
                + ", length=" + length + ", expected=" + expected + ", responded=" + responded + ", maxTopProposals="
                + maxTopProposals + "]";
    }

    public void setExpected(int expected) {
        this.expected = expected;
    }

    public void responseReceived() {
        responded++;
    }

    public boolean allResponded() {
        return expected == responded;
    }
}
