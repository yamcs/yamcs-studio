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

import org.yamcs.studio.autocomplete.IAutoCompleteProvider;

/**
 * Common content types, can be extended to define specific content types. Used by {@link IContentParser} &
 * {@link IAutoCompleteProvider} to filter content.
 */
public class ContentType {

    public static ContentType Empty = new ContentType("Empty");
    public static ContentType Undefined = new ContentType("Undefined");
    public static ContentType FormulaFunction = new ContentType("FormulaFunction");
    public static ContentType PV = new ContentType("PV");
    public static ContentType PVName = new ContentType("PVName");
    public static ContentType PVField = new ContentType("PVField");
    public static ContentType PVParam = new ContentType("PVParam");
    public static ContentType PVDataSource = new ContentType("PVDataSource");

    private final String value;

    protected ContentType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
