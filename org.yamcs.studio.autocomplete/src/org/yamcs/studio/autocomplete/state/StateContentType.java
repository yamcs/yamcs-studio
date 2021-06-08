package org.yamcs.studio.autocomplete.state;

import org.yamcs.studio.autocomplete.parser.ContentType;

public class StateContentType extends ContentType {

    public static StateContentType StateFunction = new StateContentType("StateFunction");

    private StateContentType(String value) {
        super(value);
    }
}
