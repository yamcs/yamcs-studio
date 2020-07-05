package org.csstudio.autocomplete.pvmanager.state;

import org.csstudio.autocomplete.parser.ContentType;

public class StateContentType extends ContentType {

    public static StateContentType StateFunction = new StateContentType("StateFunction");

    private StateContentType(String value) {
        super(value);
    }
}
