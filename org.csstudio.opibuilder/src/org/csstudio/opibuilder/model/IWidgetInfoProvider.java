/********************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.model;

/**
 * Use this adapter to provide widget specific information to other widgets. Sometimes a widget may want to know the
 * information of another widget. For example, the array widget want to know which property should be unique for each
 * child. Use this adapter can help to decouple their strong connections. They only need to know the same key value.
 */
public interface IWidgetInfoProvider {

    public Object getInfo(String key);

}
