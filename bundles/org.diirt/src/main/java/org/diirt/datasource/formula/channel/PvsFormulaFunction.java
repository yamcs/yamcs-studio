/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.channel;

/**
 * Formula function that accepts a list of strings and returns a table where
 * each row is the value of the channel matching the name.
 *
 * @author carcassi
 */
public class PvsFormulaFunction extends ChannelsFormulaFunction {

    @Override
    public String getName() {
        return "pvs";
    }

}
