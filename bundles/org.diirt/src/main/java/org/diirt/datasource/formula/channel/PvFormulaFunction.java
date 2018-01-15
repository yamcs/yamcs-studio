/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.channel;

/**
 * Formula function that returns the value of a channel matching the name
 * of the argument.
 *
 * @author carcassi
 */
public class PvFormulaFunction extends ChannelFormulaFunction {

    @Override
    public String getName() {
        return "pv";
    }

}
