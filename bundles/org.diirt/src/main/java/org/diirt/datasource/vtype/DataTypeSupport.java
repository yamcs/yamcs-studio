/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.vtype;

import org.diirt.vtype.VType;
import org.diirt.datasource.NotificationSupport;
import org.diirt.datasource.TypeSupport;

/**
 * Adds support for control system standard value types.
 *
 * @author carcassi
 */
public final class DataTypeSupport {

    private static boolean installed = false;

    /**
     * Installs type support. This should only be called by either DataSources
     * or ExpressionLanguage libraries that require support for these types.
     */
    public static void install() {
        // Install only once
        if (installed) {
            return;
        }

        // Add notification support for all immutable types
        TypeSupport.addTypeSupport(NotificationSupport.immutableTypeSupport(VType.class));

        installed = true;
    }

    /**
     * Constructor.
     */
    private DataTypeSupport() {
        // Don't instantiate, utility class.
    }
}
