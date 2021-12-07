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

public abstract class Column {

    private final String name;
    private final Class<?> type;
    private final boolean generated;

    public Column(String name, Class<?> type, boolean generated) {
        this.name = name;
        this.type = type;
        this.generated = generated;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isGenerated() {
        return generated;
    }

    public abstract Object getData(int size);
}
