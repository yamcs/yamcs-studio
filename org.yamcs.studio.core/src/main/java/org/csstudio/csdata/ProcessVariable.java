/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.csdata;

import java.io.Serializable;
import java.util.Objects;

/**
 * Control System Process Variable Name
 *
 * Allows Drag-and-Drop to transfer PV names, can be used for context menu object contributions.
 *
 * All control system model items must serialize for Drag-and-Drop. They should be immutable. They should implement
 * proper <code>equals()</code> and <code>hashCode()</code> to support collections.
 */
public class ProcessVariable implements Serializable {

    final private static long serialVersionUID = 1L;

    /** Process Variable name */
    private final String name;

    public ProcessVariable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Empty name");
        }
        this.name = name;
    }

    /** @return Process Variable Name */
    public String getName() {
        return name;
    }

    /**
     * Determine hash code from name
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Check equality by name
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProcessVariable)) {
            return false;
        }
        var other = (ProcessVariable) obj;
        return name.equals(other.getName());
    }

    @Override
    public String toString() {
        return "ProcessVariable '" + name + "'";
    }
}
