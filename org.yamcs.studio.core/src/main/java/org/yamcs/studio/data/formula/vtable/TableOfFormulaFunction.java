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
import org.yamcs.studio.data.vtype.Column;
import org.yamcs.studio.data.vtype.VTable;
import org.yamcs.studio.data.vtype.VTableFactory;

class TableOfFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "tableOf";
    }

    @Override
    public String getDescription() {
        return "Constructs a table from a series of columns";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(Column.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("columns");
    }

    @Override
    public Class<?> getReturnType() {
        return VTable.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        List<Object> argsNoNull = new ArrayList<>(args);

        // Remove null columns if there are any
        var removedNull = false;
        while (argsNoNull.remove(null)) {
            removedNull = true;
        }

        // If null was removed, check whether all the remaining columns
        // are generated. In that case, return null.
        // This needs to be here because ListNumberProvider are usually
        // static, while the other columns may be from waveforms coming from
        // the network. So, at connection, it's often the case
        // that only variable columns are connected. This is a temporary
        // problem, so we don't want the warning that at least
        // one column must be fixed size.
        if (removedNull) {
            var allGenerated = true;
            for (var object : argsNoNull) {
                var column = (Column) object;
                if (!column.isGenerated()) {
                    allGenerated = false;
                }
            }
            if (allGenerated) {
                return null;
            }
        }

        var columns = argsNoNull.toArray(new Column[argsNoNull.size()]);

        return VTableFactory.newVTable(columns);
    }
}
