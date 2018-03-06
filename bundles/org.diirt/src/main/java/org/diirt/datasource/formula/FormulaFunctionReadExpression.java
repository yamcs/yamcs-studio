/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import org.diirt.datasource.PVDirector;
import org.diirt.datasource.ReadRecipeBuilder;
import org.diirt.datasource.expression.DesiredRateExpressionImpl;
import org.diirt.datasource.expression.DesiredRateExpressionList;

/**
 *
 * @author carcassi
 */
class FormulaFunctionReadExpression extends DesiredRateExpressionImpl<Object> {

    public FormulaFunctionReadExpression(DesiredRateExpressionList<?> childExpressions, FormulaReadFunction function, String defaultName) {
        super(childExpressions, function, defaultName);
    }

    @Override
    public void fillReadRecipe(PVDirector director, ReadRecipeBuilder builder) {
        super.fillReadRecipe(director, builder);
        ((FormulaReadFunction) getFunction()).setDirector(director);
    }

}
