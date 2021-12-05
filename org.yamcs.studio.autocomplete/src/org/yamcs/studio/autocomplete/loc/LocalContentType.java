/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.loc;

import org.yamcs.studio.autocomplete.parser.ContentType;

public class LocalContentType extends ContentType {

    public static LocalContentType LocalPV = new LocalContentType("LocalPV");

    private LocalContentType(String value) {
        super(value);
    }

}
