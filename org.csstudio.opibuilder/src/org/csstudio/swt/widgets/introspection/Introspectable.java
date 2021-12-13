/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.introspection;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;

/**
 * An introspectable object can give its bean information. All CSS widgets in this library must implement this
 * interface.
 */
public interface Introspectable {

    BeanInfo getBeanInfo() throws IntrospectionException;
}
