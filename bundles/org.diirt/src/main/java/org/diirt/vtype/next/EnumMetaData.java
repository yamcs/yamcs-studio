/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import java.util.List;

/**
 * Enum metadata information.
 *
 * @author carcassi
 */
public abstract class EnumMetaData {

    /**
     * Returns the possible labels for the enum.
     *
     * @return the labels; not null
     */
    public abstract List<String> getLabels();

    /**
     * Whether the given object is an EnumMetadata with the same labels.
     *
     * @param obj another alarm
     * @return true if equal
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof EnumMetaData) {
            EnumMetaData other = (EnumMetaData) obj;

            return getLabels().equals(other.getLabels());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        return getLabels().hashCode();
    }

    @Override
    public final String toString() {
        return getLabels().toString();
    }

    /**
     * New EnumMetaData with the given labels.
     *
     * @param labels the enum labels
     * @return the new alarm
     */
    public static EnumMetaData create(final List<String> labels) {
        return new IEnumMetaData(labels);
    }

}
