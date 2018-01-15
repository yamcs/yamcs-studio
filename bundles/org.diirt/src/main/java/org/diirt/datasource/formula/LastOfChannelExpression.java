/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import java.util.List;
import org.diirt.datasource.ReadRecipeBuilder;
import org.diirt.datasource.ReadFunction;
import org.diirt.datasource.expression.DesiredRateExpression;
import org.diirt.datasource.expression.DesiredRateExpressionImpl;
import org.diirt.datasource.expression.DesiredRateExpressionList;
import static org.diirt.datasource.ExpressionLanguage.*;
import org.diirt.datasource.PVDirector;

/**
 *
 * @author carcassi
 */
class LastOfChannelExpression<T> implements DesiredRateExpression<T> {

    private final DesiredRateExpression<T> expression;
    private final Class<T> clazz;

    public LastOfChannelExpression(String name, Class<T> clazz) {
        this.expression = latestValueOf(channel(name, clazz, Object.class));
        this.clazz = clazz;
    }

    @Override
    public DesiredRateExpression<T> as(String name) {
        return expression.as(name);
    }

    @Override
    public String getName() {
        return expression.getName();
    }

    @Override
    public void fillReadRecipe(PVDirector director, ReadRecipeBuilder builder) {
        expression.fillReadRecipe(director, builder);
    }

    @Override
    public ReadFunction<T> getFunction() {
        return expression.getFunction();
    }

    @Override
    public DesiredRateExpressionImpl<T> getDesiredRateExpressionImpl() {
        return expression.getDesiredRateExpressionImpl();
    }

    @Override
    public DesiredRateExpressionList<T> and(DesiredRateExpressionList<? extends T> expressions) {
        return expression.and(expressions);
    }

    @Override
    public List<DesiredRateExpression<T>> getDesiredRateExpressions() {
        return expression.getDesiredRateExpressions();
    }

    public <N> LastOfChannelExpression<N> cast(Class<N> clazz) {
        if (clazz.isAssignableFrom(this.clazz)) {
            @SuppressWarnings("unchecked")
            LastOfChannelExpression<N> result = (LastOfChannelExpression<N>) this;
            return result;
        }

        if (this.clazz.isAssignableFrom(clazz)) {
            return new LastOfChannelExpression<N>(getName(), clazz);
        }

        throw new IllegalArgumentException("Cannot cast expression of type " + this.clazz + " to type " + clazz);
    }

    public Class<T> getType() {
        return clazz;
    }

}
