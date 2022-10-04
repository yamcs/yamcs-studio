/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.loc;

import java.io.IOException;
import java.util.regex.Pattern;

import org.yamcs.studio.autocomplete.AutoCompleteConstants;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.IContentParser;
import org.yamcs.studio.autocomplete.parser.engine.ExprLexer;
import org.yamcs.studio.autocomplete.parser.engine.ExprToken;
import org.yamcs.studio.autocomplete.parser.engine.ExprTokenType;

/**
 * Local Data Source content parser.
 */
public class LocalContentParser implements IContentParser {

    public static final String LOCAL_SOURCE = "loc://";
    public static final String VTYPE_START = "<";
    public static final String VTYPE_END = ">";
    public static final String INITIAL_VALUE_START = "(";
    public static final String INITIAL_VALUE_END = ")";

    private LocalContentDescriptor currentDescriptor;
    private String contentToParse;

    @Override
    public boolean accept(ContentDescriptor desc) {
        if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX)) {
            return false;
        }
        return desc.getValue().startsWith(LOCAL_SOURCE);
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        var startIndex = 0;
        contentToParse = desc.getValue();
        if (contentToParse.startsWith(LOCAL_SOURCE)) {
            contentToParse = contentToParse.substring(LOCAL_SOURCE.length());
            // startIndex = LOCAL_SOURCE.length();
        }
        currentDescriptor = new LocalContentDescriptor();
        currentDescriptor.setContentType(LocalContentType.LocalPV);
        currentDescriptor.setStartIndex(startIndex);
        currentDescriptor.setValue(contentToParse);
        parseLocContent(contentToParse);
        return currentDescriptor;
    }

    private void parseLocContent(String locContent) {
        String pvName = null;
        String vType = null;

        // handle VType
        var ltIndex = locContent.indexOf(VTYPE_START);
        var gtIndex = locContent.indexOf(VTYPE_END);
        if (ltIndex > 0) { // pvname<
            pvName = locContent.substring(0, ltIndex);
            if (gtIndex > 0 && gtIndex > ltIndex) {
                vType = locContent.substring(ltIndex + 1, gtIndex);
            } else { // complete VType
                vType = locContent.substring(ltIndex + 1);
                // currentDescriptor.setStartIndex(currentDescriptor.getStartIndex() + ltIndex + 1);
                currentDescriptor.setCompletingVType(true);
            }
        }
        currentDescriptor.setvType(vType == null ? null : vType.trim());

        // handle initialValue (ignore macros)
        var pattern = Pattern.compile("[^\\$]\\(");
        var matcher = pattern.matcher(locContent);
        if (matcher.find()) {
            currentDescriptor.setCompletingInitialValue(true);
            if (pvName == null) {
                pvName = locContent.substring(0, matcher.start() + 1);
            }
            try {
                parseInitialValues(locContent.substring(matcher.start() + 1));
            } catch (IOException ex) {
                // TODO something
            }
        }
        currentDescriptor.setPvName(pvName == null ? locContent.trim() : pvName.trim());
    }

    private void parseInitialValues(String content) throws IOException {
        ExprToken e = null;
        var lexer = new ExprLexer(content);
        while ((e = lexer.next()) != null) {
            if (e.type == ExprTokenType.Comma) {
                continue;
            }
            if (e.type == ExprTokenType.CloseBracket) {
                currentDescriptor.setComplete(true);
                break;
            }
            switch (e.type) {
            case Decimal:
                currentDescriptor.addInitialvalue(e.val, Double.class);
                break;
            case Integer:
                currentDescriptor.addInitialvalue(e.val, Double.class);
                break;
            case String:
                currentDescriptor.addInitialvalue(e.val, String.class);
                break;
            default:
                break;
            }
        }
    }
}
