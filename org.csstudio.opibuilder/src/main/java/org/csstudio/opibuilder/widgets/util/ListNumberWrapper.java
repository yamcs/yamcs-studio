/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.util;

import org.csstudio.swt.widgets.datadefinition.IPrimaryArrayWrapper;
import org.yamcs.studio.data.vtype.ListNumber;

/**
 * An {@link IPrimaryArrayWrapper} for {@link ListNumber}
 */
public class ListNumberWrapper implements IPrimaryArrayWrapper {

    private ListNumber listNumber;

    public ListNumberWrapper(ListNumber listNumber) {
        this.listNumber = listNumber;
    }

    @Override
    public double get(int i) {
        return listNumber.getDouble(i);
    }

    @Override
    public int getSize() {
        return listNumber.size();
    }
}
