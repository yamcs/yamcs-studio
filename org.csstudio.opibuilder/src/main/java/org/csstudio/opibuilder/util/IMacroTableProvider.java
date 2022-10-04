/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

/**
 * Provides value for a macro.
 */
public interface IMacroTableProvider {
    /**
     * Get value of a macro.
     *
     * @param macroName
     *            the name of the macro
     * @return the value of the macro, null if no such macro exists.
     */
    String getMacroValue(String macroName);
}
