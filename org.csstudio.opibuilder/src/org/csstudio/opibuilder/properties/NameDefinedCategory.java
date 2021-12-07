/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties;

/**
 * A property category whose name is specified from the constructor input.
 */
public class NameDefinedCategory implements WidgetPropertyCategory {
    private String name;

    public NameDefinedCategory(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
