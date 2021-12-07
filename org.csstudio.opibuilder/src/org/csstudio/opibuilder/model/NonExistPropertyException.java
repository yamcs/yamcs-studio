/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.model;

/**
 * The exception shows that the property doesn't exist.
 */
public class NonExistPropertyException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String propID;
    private String widgetName;

    public NonExistPropertyException(String widgetName, String propID) {
        this.propID = propID;
        this.widgetName = widgetName;
    }

    @Override
    public String getMessage() {
        return widgetName + " does not have property: " + propID;
    }
}
