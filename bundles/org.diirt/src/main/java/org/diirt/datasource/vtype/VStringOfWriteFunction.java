/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.vtype;

import org.diirt.vtype.ValueFormat;
import org.diirt.vtype.VType;
import org.diirt.datasource.ReadFunction;
import org.diirt.datasource.WriteFunction;

/**
 * Converts the value of the argument to a VString.
 *
 * @author carcassi
 */
class VStringOfWriteFunction implements WriteFunction<String> {

    private final ReadFunction<? extends VType> reference;
    private final WriteFunction<Object> argument;
    private final ValueFormat format;

    public VStringOfWriteFunction(ReadFunction<? extends VType> reference, ValueFormat format, WriteFunction<Object> argument) {
        this.reference = reference;
        this.format = format;
        this.argument = argument;
    }

    @Override
    public void writeValue(String newValue) {
        argument.writeValue(format.parseObject(newValue, reference.readValue()));
    }

}
