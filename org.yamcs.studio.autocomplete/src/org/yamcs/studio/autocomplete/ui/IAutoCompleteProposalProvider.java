/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui;

import org.yamcs.studio.autocomplete.ui.content.ContentProposalAdapter;
import org.yamcs.studio.autocomplete.ui.content.IContentProposalSearchHandler;

/**
 * Provides auto-completion results to {@link ContentProposalAdapter}.
 */
public interface IAutoCompleteProposalProvider {

    /**
     * Requests providers for proposals and notify the handler each time a provider answers.
     *
     * @param contents
     *            the content to complete.
     * @param handler
     *            see {@link IContentProposalSearchHandler}.
     */
    public void getProposals(String contents, IContentProposalSearchHandler handler);

    /** @return <code>true</code> if at least one provider is defined. */
    public boolean hasProviders();

    /** Cancel the current request. */
    public void cancel();

    /** @return current type, see {@link AutoCompleteTypes}. */
    public String getType();

}
