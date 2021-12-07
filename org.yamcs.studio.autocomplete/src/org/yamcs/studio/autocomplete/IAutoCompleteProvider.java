/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete;

import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;

/**
 * Interface for auto-complete providers. Each parser is provided via OSGI services. The listResult method is executed
 * by {@link AutoCompleteService} in a dedicated thread.
 */
public interface IAutoCompleteProvider {

    /** @return <code>true</code> if provider handles this type of content */
    public boolean accept(ContentType type);

    /**
     * @return {@link AutoCompleteResult} matching the provided {@link ContentDescriptor}
     */
    public AutoCompleteResult listResult(ContentDescriptor desc, int limit);

    /**
     * Called by {@link AutoCompleteService} when the task is canceled.
     */
    public void cancel();

}
