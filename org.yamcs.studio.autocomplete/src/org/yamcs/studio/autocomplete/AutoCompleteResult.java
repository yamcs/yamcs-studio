/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.tooltips.TooltipData;
import org.yamcs.studio.autocomplete.tooltips.TooltipDataHandler;

/**
 * Result provided by {@link IAutoCompleteProvider}.
 */
public class AutoCompleteResult {

    /**
     * Provided proposals that will be displayed by provider in the main pop-up.
     */
    private List<Proposal> proposals;
    /**
     * Top proposals that will be handled by the top proposal manager and displayed on top of the main pop-up.
     */
    private List<Proposal> topProposals;
    /**
     * Data that will be processed by {@link TooltipDataHandler} to display a tool-tip each time a new result is
     * provided.
     */
    private List<TooltipData> tooltips;
    /**
     * Total matching results count displayed next to the provider name.
     */
    private int count;
    /**
     * Provider name, if <code>null</code> provider will not be display, only top proposals.
     */
    private String provider;

    public AutoCompleteResult() {
        this.proposals = new LinkedList<Proposal>();
        this.topProposals = new LinkedList<Proposal>();
        this.tooltips = new LinkedList<TooltipData>();
        this.count = 0;
    }

    public void addProposal(Proposal p) {
        proposals.add(p);
    }

    public void addTopProposal(Proposal p) {
        topProposals.add(p);
    }

    public void addTooltipData(TooltipData td) {
        tooltips.add(td);
    }

    public List<String> getProposalsAsString() {
        List<String> strList = new ArrayList<>();
        for (var p : proposals) {
            strList.add(p.getValue());
        }
        return strList;
    }

    public List<String> getTopProposalsAsString() {
        List<String> strList = new ArrayList<>();
        for (var p : topProposals) {
            strList.add(p.getValue());
        }
        return strList;
    }

    public List<Proposal> getProposals() {
        return proposals;
    }

    public List<Proposal> getTopProposals() {
        return topProposals;
    }

    public List<TooltipData> getTooltips() {
        return tooltips;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
