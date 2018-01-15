/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import org.diirt.datasource.PVDirector;
import org.diirt.datasource.ReadFunction;
import org.diirt.datasource.ReadRecipeBuilder;
import org.diirt.datasource.expression.DesiredRateExpressionImpl;
import org.diirt.datasource.expression.DesiredRateExpressionListImpl;

/**
 *
 * @author carcassi
 */
class ErrorDesiredRateExpression<T> extends DesiredRateExpressionImpl<T> {
    private final RuntimeException error;

    public ErrorDesiredRateExpression(final RuntimeException ex, String defaultName) {
        super(new DesiredRateExpressionListImpl<>(), new ReadFunction<T>() {
            @Override
            public T readValue() {
                return null;
            }
        }, defaultName);
        this.error = ex;
    }

    @Override
    public void fillReadRecipe(PVDirector director, ReadRecipeBuilder builder) {
        super.fillReadRecipe(director, builder); //To change body of generated methods, choose Tools | Templates.
        director.connectStaticRead(error, false, getName());
    }

}
