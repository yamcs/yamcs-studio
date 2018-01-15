/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.channel;

import org.diirt.datasource.formula.FormulaFunctionSet;
import org.diirt.datasource.formula.FormulaFunctionSetDescription;

/**
 * A set of functions to work with channels.
 *
 * @author carcassi
 *
 */
public class ChannelFunctionSet extends FormulaFunctionSet {

    /**
     * Creates a new set.
     */
    public ChannelFunctionSet() {
        super(new FormulaFunctionSetDescription("channel",
                "Functions to work on channels")
                .addFormulaFunction(new ChannelFormulaFunction())
                .addFormulaFunction(new ChannelsFormulaFunction())
                .addFormulaFunction(new PvFormulaFunction())
                .addFormulaFunction(new PvsFormulaFunction())
                );
    }

}
