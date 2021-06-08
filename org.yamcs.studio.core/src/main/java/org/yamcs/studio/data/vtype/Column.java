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
