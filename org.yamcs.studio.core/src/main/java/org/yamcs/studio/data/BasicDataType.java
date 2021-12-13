/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data;

public enum BasicDataType {

    BOOLEAN("boolean"),
    CHAR("char"),
    BYTE("byte"),
    SHORT("short"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    STRING("String"),
    TIMESTAMP("Timestamp"),
    ENUM("enum"),
    UNKNOWN("unknown"),
    BOOLEAN_ARRAY("boolean[]"),
    CHAR_ARRAY("char[]"),
    BYTE_ARRAY("byte[]"),
    SHORT_ARRAY("short[]"),
    INT_ARRAY("int[]"),
    LONG_ARRAY("long[]"),
    FLOAT_ARRAY("float[]"),
    DOUBLE_ARRAY("double[]"),
    STRING_ARRAY("String[]"),
    ENUM_ARRAY("enum[]"),
    OBJECT_ARRAY("Object[]");

    private String description;

    private BasicDataType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static String[] stringValues() {
        var result = new String[values().length];
        var i = 0;
        for (var f : values()) {
            result[i++] = f.toString();
        }
        return result;
    }
}
