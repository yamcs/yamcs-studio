/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.proposals;

import org.eclipse.swt.SWT;

/**
 * Used to define a SWT StyleRange on proposal display.
 */
public class ProposalStyle {

    /** Start index. */
    public int from;
    /** End index. */
    public int to;
    /** SWT Font style */
    public int fontStyle;
    /** SWT Color constant. */
    public int fontColor;

    public ProposalStyle(int from, int to, int fontStyle, int fontColor) {
        this.from = from;
        this.to = to;
        this.fontStyle = fontStyle;
        this.fontColor = fontColor;
    }

    public ProposalStyle(ProposalStyle ps) {
        from = ps.from;
        to = ps.to;
        fontStyle = ps.fontStyle;
        fontColor = ps.fontColor;
    }

    public static ProposalStyle getDefault(int from, int to) {
        return new ProposalStyle(from, to, SWT.BOLD, SWT.COLOR_BLUE);
    }

    public static ProposalStyle getError(int from, int to) {
        return new ProposalStyle(from, to, SWT.BOLD, SWT.COLOR_RED);
    }

    public static ProposalStyle getItalic(int from, int to) {
        return new ProposalStyle(from, to, SWT.ITALIC, SWT.COLOR_GRAY);
    }
}
