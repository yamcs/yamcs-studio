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
