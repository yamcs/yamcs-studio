/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import org.yamcs.protobuf.Mdb.ArgumentInfo;

/**
 * An argument to a telecommand, whether that's assigned or not -- doesn't matter
 */
public class TelecommandArgument {

    private ArgumentInfo argumentInfo;
    private String value;
    private boolean editable;

    public TelecommandArgument(ArgumentInfo argumentInfo, boolean editable) {
        this.argumentInfo = argumentInfo;
        this.editable = editable;
        value = argumentInfo.hasInitialValue() ? argumentInfo.getInitialValue() : null;
    }

    public String getName() {
        return argumentInfo.getName();
    }

    public String getType() {
        return argumentInfo.getType().getEngType();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ArgumentInfo getArgumentInfo() {
        return argumentInfo;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
