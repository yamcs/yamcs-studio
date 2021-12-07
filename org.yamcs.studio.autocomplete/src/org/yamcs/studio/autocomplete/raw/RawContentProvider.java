/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.autocomplete.raw;

import org.yamcs.studio.autocomplete.para.ParameterContentProvider;

public class RawContentProvider extends ParameterContentProvider {

    @Override
    public String getPrefix() {
        return RawContentParser.RAW_SOURCE;
    }

    @Override
    public boolean requirePrefix() {
        return true;
    }
}
