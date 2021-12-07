/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

import java.util.List;

/**
 * Metadata for enumerations.
 */
public interface Enum {

    /**
     * All the possible labels. Never null.
     *
     * @return the possible values
     */
    List<String> getLabels();
}
