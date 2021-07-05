package org.yamcs.studio.data.vtype;

public abstract class ListNumberProvider {

    private final Class<?> type;

    public ListNumberProvider(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public abstract ListNumber createListNumber(int size);
}
