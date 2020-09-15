/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.yamcs.studio.autocomplete.parser.engine.expr;

public class ExprFunction extends Expr {

    private String name;
    private Expr[] args;
    private Object annotation;
    private boolean isComplete = false;

    public ExprFunction(String name, Expr[] args) {
        super(ExprType.Function);
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        return args.length;
    }

    public Expr getArg(int index) {
        return args[index];
    }

    public Expr[] getArgs() {
        return args;
    }

    public void setAnnotation(Object annotation) {
        this.annotation = annotation;
    }

    public Object getAnnotation() {
        return annotation;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(args[i]);
        }
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws ExprException {
        if (name == null)
            throw new ExprException("Function name cannot be empty");
        for (int i = 0; i < args.length; i++) {
            args[i].validate();
        }
    }
}
