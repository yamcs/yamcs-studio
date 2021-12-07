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
        for (BasicDataType f : values()) {
            result[i++] = f.toString();
        }
        return result;
    }
}
