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

public abstract class ListNumberProvider {

    private final Class<?> type;

    public ListNumberProvider(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public abstract ListNumber createListNumber(int size);
}
