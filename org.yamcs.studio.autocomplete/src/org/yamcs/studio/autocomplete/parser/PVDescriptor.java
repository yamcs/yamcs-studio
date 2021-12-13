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

import java.util.HashMap;
import java.util.Map;

import org.yamcs.studio.autocomplete.IAutoCompleteProvider;

/**
 * Descriptor used in {@link IContentParser} and {@link IAutoCompleteProvider} to describe a content matching a PV.
 */
public class PVDescriptor extends ContentDescriptor {

    private String name;
    private String field;
    private Map<String, String> params;

    public PVDescriptor() {
        params = new HashMap<>();
    }

    public void addParam(String name, String value) {
        params.put(name, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "PVDescriptor [name=" + name + ", field=" + field + ", params=" + params + ", toString()="
                + super.toString() + "]";
    }
}
