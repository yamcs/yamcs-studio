/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.sim;

import org.yamcs.studio.autocomplete.parser.ContentType;

public class SimContentType extends ContentType {

    public static SimContentType SimFunction = new SimContentType("SimFunction");

    private SimContentType(String value) {
        super(value);
    }

}
