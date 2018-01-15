/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.ndarray;

import org.diirt.util.array.ListInt;
import org.diirt.util.array.ListNumber;

/**
 *
 * @author carcassi
 */
public abstract class InvertListNumber implements ListNumber {

    static void fillCoords(int[] coords, int index, ListInt sizes) {
        for (int i = 0; i < coords.length; i++) {
            int revI = sizes.size() - i - 1;
            int size = sizes.getInt(revI);
            coords[revI] = index % size;
            index = index / size;
        }
    }

    static int index(int[] coords, ListInt sizes, boolean[] invert) {
        int index = 0;
        for (int i = 0; i < coords.length; i++) {
            index *= sizes.getInt(i);
            if (!invert[i]) {
                index += coords[i];
            } else {
                index += sizes.getInt(i) - coords[i] - 1;
            }
        }
        return index;
    }

}
