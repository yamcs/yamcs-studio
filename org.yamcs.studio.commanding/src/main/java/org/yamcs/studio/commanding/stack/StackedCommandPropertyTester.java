/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import org.eclipse.core.expressions.PropertyTester;

public class StackedCommandPropertyTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        var cmd = (StackedCommand) receiver;
        if ("canRun".equals(property)) {
            return canRun(cmd);
        }
        return false;
    }

    private boolean canRun(StackedCommand cmd) {
        return cmd.isArmed();
    }
}
