/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.tooltips;

import org.yamcs.studio.autocomplete.proposals.ProposalStyle;

/**
 * Content that will be displayed in UI. Result of concatenation of all provided {@link TooltipData}.
 */
public class TooltipContent {

    /** Multiple-lines value that will be displayed in UI. */
    public String value;
    /** SWT StyleRange that will be applied to value. */
    public ProposalStyle[] styles;
    public int maxLineLength;
    public int numberOfLines;

}
