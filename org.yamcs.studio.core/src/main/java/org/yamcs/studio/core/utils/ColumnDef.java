package org.yamcs.studio.core.utils;

import java.util.Objects;

public class ColumnDef {
    public final String name;
    final int keyIndex;

    public int newIndex;
    public int width;

    public boolean visible;
    public boolean moveable;
    public boolean resizable;

    public ColumnDef(String name, int currIndex) {
        this.name = name;
        this.keyIndex = currIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, keyIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ColumnDef)) {
            return false;
        }
        var other = (ColumnDef) obj;
        return Objects.equals(name, other.name) && Objects.equals(keyIndex, other.keyIndex);
    }

    @Override
    public String toString() {
        return name;
    }
}
