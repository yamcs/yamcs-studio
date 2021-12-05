/*******************************************************************************
 * Copyright (c) 2010-2016 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.yamcs.studio.autocomplete.sim;

import org.yamcs.studio.autocomplete.AutoCompleteConstants;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentParserHelper;
import org.yamcs.studio.autocomplete.parser.FunctionDescriptor;
import org.yamcs.studio.autocomplete.parser.IContentParser;

/**
 * Simulation Data Source content parser.
 *
 * @author Fred Arnaud (Sopra Group)
 */
public class SimContentParser implements IContentParser {

    public static final String SIM_SOURCE = "sim://";

    @Override
    public boolean accept(final ContentDescriptor desc) {
        if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX))
            return false;
        if (desc.getValue().startsWith(SIM_SOURCE)
                || (desc.getValue().indexOf(AutoCompleteConstants.DATA_SOURCE_NAME_SEPARATOR) == -1
                        && SIM_SOURCE.equals(desc.getDefaultDataSource())))
            return true;
        return false;
    }

    @Override
    public ContentDescriptor parse(final ContentDescriptor desc) {
        int startIndex = 0;
        String contentToParse = desc.getValue();
        if (contentToParse.startsWith(SIM_SOURCE)) {
            contentToParse = contentToParse.substring(SIM_SOURCE.length());
            // startIndex = SIM_SOURCE.length();
        }
        FunctionDescriptor currentDesc = null;
        if (contentToParse
                .contains(AutoCompleteConstants.WILDCARD_MULTI_REPLACE)
                || contentToParse
                        .contains(AutoCompleteConstants.WILDCARD_SINGLE_REPLACE)) {
            currentDesc = new FunctionDescriptor();
            currentDesc.setFunctionName(contentToParse);
        } else {
            currentDesc = ContentParserHelper
                    .parseStandardFunction(contentToParse);
        }
        currentDesc.setContentType(SimContentType.SimFunction);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }

}
