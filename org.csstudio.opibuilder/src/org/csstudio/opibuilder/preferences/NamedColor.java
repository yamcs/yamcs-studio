/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.preferences;

import org.eclipse.swt.graphics.RGB;

public class NamedColor {

    public String name;
    public RGB rgb;

    public NamedColor(String name, RGB rgb) {
        this.name = name;
        this.rgb = rgb;
    }
}
