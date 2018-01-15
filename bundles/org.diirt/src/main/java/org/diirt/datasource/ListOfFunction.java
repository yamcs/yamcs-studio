/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * A function that takes many inputs and creates a list with them.
 *
 * @author carcassi
 */
class ListOfFunction implements ReadFunction<List> {

    List<ReadFunction> functions;

    public ListOfFunction(List<ReadFunction> functions) {
        this.functions = functions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List readValue() {
        List list = new ArrayList();
        for (ReadFunction function : functions) {
            list.add(function.readValue());
        }
        return list;
    }

}
