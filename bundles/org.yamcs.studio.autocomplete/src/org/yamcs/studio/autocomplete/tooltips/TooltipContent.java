/*******************************************************************************
 * Copyright (c) 2010-2016 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.yamcs.studio.autocomplete.tooltips;

import org.yamcs.studio.autocomplete.proposals.ProposalStyle;

/**
 * Content that will be displayed in UI. Result of concatenation of all provided
 * {@link TooltipData}.
 *
 * @author Fred Arnaud (Sopra Group) - ITER
 */
public class TooltipContent {

    /** Multiple-lines value that will be displayed in UI. */
    public String value;
    /** SWT StyleRange that will be applied to value. */
    public ProposalStyle[] styles;
    public int maxLineLength;
    public int numberOfLines;

}
