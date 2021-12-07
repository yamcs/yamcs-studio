/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2012, 2021 diirt developers and others
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.yamcs.studio.data.formula.vtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VTable;
import org.yamcs.studio.data.vtype.VTableFactory;

/**
 * Natural join of a set of tables.
 */
class NaturalJoinFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Natural join between tables";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VTable.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("tables");
    }

    @Override
    public Class<?> getReturnType() {
        return VTable.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        List<VTable> tables = new ArrayList<>();
        for (Object object : args) {
            if (object != null) {
                tables.add((VTable) object);
            }
        }

        return VTableFactory.join(tables);
    }

}
