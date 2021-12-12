/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.autocomplete.state;

import org.yamcs.studio.autocomplete.AutoCompleteConstants;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.IContentParser;

public class StateContentParser implements IContentParser {

    public static final String STATE_SOURCE = "state://";

    @Override
    public boolean accept(ContentDescriptor desc) {
        if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX)) {
            return false;
        }
        return desc.getValue().startsWith(STATE_SOURCE);
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        var startIndex = 0;
        var contentToParse = desc.getValue();
        if (contentToParse.startsWith(STATE_SOURCE)) {
            contentToParse = contentToParse.substring(STATE_SOURCE.length());
        }
        var currentDesc = new StateContentDescriptor();
        currentDesc.setContentType(StateContentType.StateFunction);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }
}
