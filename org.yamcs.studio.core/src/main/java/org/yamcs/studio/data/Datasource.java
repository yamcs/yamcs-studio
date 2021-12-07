/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data;

import org.yamcs.studio.data.vtype.VType;

public interface Datasource {

    boolean supportsPVName(String pvName);

    boolean isConnected(IPV pv);

    boolean isWriteAllowed(IPV pv);

    VType getValue(IPV pv);

    void writeValue(IPV pv, Object value, WriteCallback callback);

    void onStarted(IPV pv);

    void onStopped(IPV pv);
}
