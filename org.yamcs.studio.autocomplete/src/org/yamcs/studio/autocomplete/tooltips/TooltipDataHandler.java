/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.tooltips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.yamcs.studio.autocomplete.proposals.ProposalStyle;

/**
 * Handles a list of {@link TooltipData} to provide a {@link TooltipContent}.
 */
public class TooltipDataHandler {

    private List<TooltipData> tooltipDataList;

    public TooltipDataHandler() {
        this.tooltipDataList = Collections.synchronizedList(new ArrayList<TooltipData>());
    }

    public void addData(TooltipData data) {
        tooltipDataList.add(data);
    }

    public void clearData() {
        tooltipDataList.clear();
    }

    public TooltipContent generateTooltipContent(String fieldContent) {
        if (tooltipDataList.isEmpty() || fieldContent == null || fieldContent.trim().isEmpty()) {
            return null; // no content
        }

        // build content
        int offset = 0, maxLineLength = 0, numberOfLines = 0;
        var sb = new StringBuilder();
        List<ProposalStyle> styleList = new ArrayList<ProposalStyle>();
        synchronized (tooltipDataList) {
            for (TooltipData data : tooltipDataList) {
                var startLength = sb.length();
                sb.append(data.value);
                sb.append("\n");
                if (data.styles != null) {
                    for (ProposalStyle style : data.styles) {
                        var ps = new ProposalStyle(style);
                        ps.from += offset;
                        ps.to += offset;
                        styleList.add(ps);
                    }
                }
                offset += sb.length() - startLength;
                maxLineLength = Math.max(maxLineLength, sb.length() - startLength);
                numberOfLines++;
            }
        }

        if (sb.length() == 0) {
            return null; // no content
        }

        // delete last \n
        sb.deleteCharAt(sb.length() - 1);

        var tc = new TooltipContent();
        tc.value = sb.toString();
        tc.styles = styleList.toArray(new ProposalStyle[styleList.size()]);
        tc.numberOfLines = numberOfLines;
        tc.maxLineLength = maxLineLength;
        return tc;
    }

}
