/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.yamcs.studio.autocomplete.AutoCompleteConstants;
import org.yamcs.studio.autocomplete.AutoCompleteHelper;
import org.yamcs.studio.autocomplete.AutoCompleteType;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.IContentParser;

/**
 * Parses content to determine if it matches a defined data source prefix.
 */
public class DataSourceParser implements IContentParser {

    private List<String> dataSources;

    public DataSourceParser() {
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
    public boolean accept(ContentDescriptor desc) {
        var content = desc.getValue();
        if (content == null || content.isEmpty()) {
            return false;
        }
        if (desc.getAutoCompleteType().equals(AutoCompleteType.PV)
                || desc.getAutoCompleteType().equals(AutoCompleteType.Formula)) {
            for (String ds : dataSources) {
                if (ds.startsWith(content) && content.length() < ds.length()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        var currentToken = new ContentDescriptor();
        currentToken.setContentType(ContentType.PVDataSource);
        currentToken.setValue(desc.getValue());
        return currentToken;
    }

}
