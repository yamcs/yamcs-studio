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

import org.yamcs.studio.autocomplete.AutoCompleteConstants;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.IContentParser;

/**
 * Yamcs Opsname DataSource content parser.
 */
public class OpsContentParser implements IContentParser {

    public static final String OPS_SOURCE = "ops://";

    @Override
    public boolean accept(ContentDescriptor desc) {
        if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX)) {
            return false;
        } else if (desc.getValue().startsWith("/")) {
            return false;
        }
        return true;
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        var startIndex = 0;
        var contentToParse = desc.getValue();
        if (contentToParse.startsWith(OPS_SOURCE)) {
            contentToParse = contentToParse.substring(OPS_SOURCE.length());
        }
        var currentDesc = new ContentDescriptor();
        currentDesc.setContentType(ContentType.PVName);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }
}
