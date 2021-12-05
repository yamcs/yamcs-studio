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

import org.yamcs.studio.autocomplete.AutoCompleteResult;
import org.yamcs.studio.autocomplete.proposals.Proposal;
import org.yamcs.studio.autocomplete.proposals.ProposalStyle;

/**
 * Part of {@link TooltipContent} provided via {@link AutoCompleteResult} or {@link Proposal}. This class represents a
 * single line of the final tool-tip content (see {@link TooltipContent}). The usage is 1 {@link TooltipData} per
 * function.
 */
public class TooltipData {

    /** Value that will be concatenated. Represents a single line. */
    public String value;
    /** SWT StyleRange that will be applied to value. */
    public ProposalStyle[] styles;

}
