package org.yamcs.studio.commanding.stack;

/**
 * An argument to a telecommand, whether that's assigned or not -- doesn't matter
 */
public class TelecommandArgument {

    private String name;
    private String value;
    private boolean editable;

    public TelecommandArgument(String name, String value, boolean editable) {
        this.name = name;
        this.value = value;
        this.editable = editable;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isEditable() {
        return editable;
    }
}
