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

import java.util.Objects;

import org.yamcs.studio.autocomplete.parser.IContentParser;

/**
 * Common types for auto-completed fields, used by {@link IContentParser} to determine if the field has to be parsed.
 */
public class AutoCompleteType {

    public static AutoCompleteType PV = new AutoCompleteType("PV");
    public static AutoCompleteType Formula = new AutoCompleteType("Formula");

    private final String value;

    protected AutoCompleteType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static AutoCompleteType valueOf(String value) {
        switch (value) {
        case "PV":
            return PV;
        case "Formula":
            return Formula;
        default:
            return new AutoCompleteType(value);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AutoCompleteType)) {
            return false;
        }
        return Objects.equals(value, ((AutoCompleteType) obj).value);
    }
}
