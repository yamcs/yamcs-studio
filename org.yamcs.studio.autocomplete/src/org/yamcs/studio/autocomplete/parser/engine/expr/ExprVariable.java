/********************************************************************************
 * Copyright (c) 2009, 2021 Peter Smith and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser.engine.expr;

import java.util.ArrayList;
import java.util.List;

public class ExprVariable extends Expr {

    private String name;
    private Object annotation;
    private Expr constantValue;

    public ExprVariable(String name) {
        super(ExprType.Variable);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAnnotation(Object annotation) {
        this.annotation = annotation;
    }

    public Object getAnnotation() {
        return annotation;
    }

    public void setConstantValue(Expr value) {
        this.constantValue = value;
    }

    public Expr getConstantValue() {
        return this.constantValue;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExprVariable)) {
            return false;
        }

        var ev = (ExprVariable) obj;
        return ev.name.equals(name);
    }

    public static ExprVariable[] findVariables(Expr expr) {
        List<ExprVariable> vars = new ArrayList<ExprVariable>();
        findVariables(expr, vars);
        return vars.toArray(new ExprVariable[0]);
    }

    public static void findVariables(Expr expr, List<ExprVariable> vars) {
        if (expr instanceof ExprFunction) {
            var f = (ExprFunction) expr;
            for (var i = 0; i < f.size(); i++) {
                findVariables(f.getArg(i), vars);
            }
        } else if (expr instanceof ExprExpression) {
            findVariables(((ExprExpression) expr).getChild(), vars);
        } else if (expr instanceof IBinaryOperator) {
            var bo = (IBinaryOperator) expr;
            findVariables(bo.getLHS(), vars);
            findVariables(bo.getRHS(), vars);
        } else if (expr instanceof ExprVariable) {
            vars.add(((ExprVariable) expr));
        }
    }

    @Override
    public void validate() throws ExprException {
        if (name == null) {
            throw new ExprException("Variable name is empty");
        }
    }
}
