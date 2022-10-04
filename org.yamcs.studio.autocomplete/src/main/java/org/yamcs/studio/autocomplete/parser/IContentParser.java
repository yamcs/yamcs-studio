/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser;

import org.yamcs.studio.autocomplete.AutoCompleteService;

/**
 * Common interface for auto-completed fields content parsers. Used by {@link AutoCompleteService} to parse field
 * content and select providers. Each parser is defined via OSGI services.
 *
 */
public interface IContentParser {

    /**
     * @return <code>true</code> if this parser handles the described content.
     */
    boolean accept(ContentDescriptor desc);

    /**
     * @return {@link ContentDescriptor} to be submitted to providers or parsers if the replay attribute is set to
     *         <code>true</code>.
     */
    ContentDescriptor parse(ContentDescriptor desc);
}
