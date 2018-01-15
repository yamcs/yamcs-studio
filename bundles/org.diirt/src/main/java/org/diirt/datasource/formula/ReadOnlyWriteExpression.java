/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import org.diirt.datasource.PVWriterDirector;
import org.diirt.datasource.WriteFunction;
import org.diirt.datasource.WriteRecipeBuilder;
import org.diirt.datasource.expression.WriteExpressionImpl;
import org.diirt.datasource.expression.WriteExpressionListImpl;

/**
 *
 * @author carcassi
 */
class ReadOnlyWriteExpression<T> extends WriteExpressionImpl<T> {
    private final String errorMessage;

    public ReadOnlyWriteExpression(final String errorMessage, String defaultName) {
        super(new WriteExpressionListImpl<>(), new WriteFunction<T>() {

            @Override
            public void writeValue(T newValue) {
                throw new RuntimeException(errorMessage);
            }
        }, defaultName);
        this.errorMessage = errorMessage;
    }

    @Override
    public void fillWriteRecipe(PVWriterDirector director, WriteRecipeBuilder builder) {
        super.fillWriteRecipe(director, builder);
        director.connectStatic(new RuntimeException(errorMessage), false, getName());
    }


}
