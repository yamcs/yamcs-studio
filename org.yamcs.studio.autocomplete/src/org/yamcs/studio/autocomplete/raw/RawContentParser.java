/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.autocomplete.raw;

import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.IContentParser;

/**
 * Yamcs raw DataSource content parser.
 */
public class RawContentParser implements IContentParser {

    public static final String RAW_SOURCE = "raw://";

    @Override
    public boolean accept(ContentDescriptor desc) {
        return desc.getValue().startsWith(RAW_SOURCE);
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        var startIndex = 0;
        var contentToParse = desc.getValue();
        if (contentToParse.startsWith(RAW_SOURCE)) {
            contentToParse = contentToParse.substring(RAW_SOURCE.length());
        }
        var currentDesc = new ContentDescriptor();
        currentDesc.setContentType(ContentType.PVName);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }
}
