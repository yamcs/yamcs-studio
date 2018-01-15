/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import java.util.List;

/**
 * Immutable EnumMetaData implementation.
 *
 * @author carcassi
 */
class IEnumMetaData extends EnumMetaData {

    private final List<String> labels;

    IEnumMetaData(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public List<String> getLabels() {
        return labels;
    }

}
