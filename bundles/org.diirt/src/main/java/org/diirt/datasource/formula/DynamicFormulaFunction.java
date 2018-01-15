/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import org.diirt.datasource.PVDirector;
import org.diirt.datasource.expression.DesiredRateExpression;

/**
 * Formula function that can dynamically add and remove access to
 * channels.
 * <p>
 * This formula function is given a director which can be used to open/close
 * expressions that read real-time data.
 *
 * @author carcassi
 */
public abstract class DynamicFormulaFunction extends StatefulFormulaFunction {

    private PVDirector<?> director;

    /**
     * The director to use to connect/disconnect live data expressions.
     *
     * @return the director
     */
    public final PVDirector<?> getDirector() {
        return director;
    }

    /**
     * Expression for the last value of the given channel, suitable to be
     * used within formula.
     * <p>
     * TODO: we need to clarify when it is better to use this method directly,
     * and when to use the normal expressions.
     *
     * @param <T> the expression type
     * @param channelName the channel name
     * @param clazz the expression type
     * @return a new expression
     */
    public final <T> DesiredRateExpression<T> channel(String channelName, Class clazz) {
        return new LastOfChannelExpression<>(channelName, clazz);
    }

    /**
     * Changes the director. This is not part of the public API: the director
     * is set by the infrastructure.
     *
     * @param director the new director
     */
    void setDirector(PVDirector<?> director) {
        this.director = director;
    }

}
