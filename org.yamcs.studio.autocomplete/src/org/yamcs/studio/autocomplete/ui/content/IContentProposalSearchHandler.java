/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui.content;

import java.util.List;

import org.yamcs.studio.autocomplete.tooltips.TooltipData;
import org.yamcs.studio.autocomplete.ui.IAutoCompleteProposalProvider;

/**
 * Handle results from {@link IAutoCompleteProposalProvider}.
 */
public interface IContentProposalSearchHandler {

    public void handleResult(ContentProposalList proposalList);

    public void handleTooltips(List<TooltipData> tooltips);
}
