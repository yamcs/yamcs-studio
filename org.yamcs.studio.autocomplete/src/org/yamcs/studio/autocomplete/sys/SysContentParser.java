/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.sys;

import org.yamcs.studio.autocomplete.AutoCompleteConstants;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.IContentParser;

/**
 * System Data Source content parser.
 */
public class SysContentParser implements IContentParser {

    public static final String SYS_SOURCE = "sys://";

    @Override
    public boolean accept(ContentDescriptor desc) {
        if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX)) {
            return false;
        }
        return desc.getValue().startsWith(SYS_SOURCE);
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        var startIndex = 0;
        var contentToParse = desc.getValue();
        if (contentToParse.startsWith(SYS_SOURCE)) {
            contentToParse = contentToParse.substring(SYS_SOURCE.length());
            // startIndex = SYS_SOURCE.length();
        }
        var currentDesc = new SysContentDescriptor();
        currentDesc.setContentType(SysContentType.SysFunction);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }

}
