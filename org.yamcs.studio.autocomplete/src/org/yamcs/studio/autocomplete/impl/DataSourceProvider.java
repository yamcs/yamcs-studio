/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.yamcs.studio.autocomplete.AutoCompleteConstants;
import org.yamcs.studio.autocomplete.AutoCompleteHelper;
import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.IAutoCompleteProvider;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;

/**
 * DataSource prefix (loc://, sim://, ...) provider. Provides only top proposals if the content match a defined data
 * source.
 */
public class DataSourceProvider implements IAutoCompleteProvider {

    public static final String NAME = "DataSources";
    private List<String> dataSources;

    public DataSourceProvider() {
        loadDataSources();
    }

    private void loadDataSources() {
        Set<String> supportedTypes = new HashSet<>();
        supportedTypes.addAll(AutoCompleteHelper.retrievePVManagerSupported());
        dataSources = new ArrayList<>();
        for (String supportedType : supportedTypes) {
            dataSources.add(supportedType + AutoCompleteConstants.DATA_SOURCE_NAME_SEPARATOR);
        }
        Collections.sort(dataSources);
    }

    @Override
    public boolean accept(ContentType type) {
        if (type == ContentType.PVDataSource) {
            return true;
        }
        return false;
    }

    @Override
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
        var result = new AutoCompleteResult();
        for (String ds : dataSources) {
            if (ds.startsWith(desc.getValue())) {
                var proposal = new Proposal(ds, true);
                proposal.addStyle(ProposalStyle.getDefault(0, desc.getValue().length() - 1));
                proposal.setInsertionPos(desc.getStartIndex());
                result.addTopProposal(proposal);
            }
        }
        return result;
    }

    @Override
    public void cancel() {
    }

}
