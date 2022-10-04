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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Mostly this is used as a placeholder for the descriptors.
 *
 */

class GenericBeanInfo extends SimpleBeanInfo {

    private BeanDescriptor beanDescriptor;
    private EventSetDescriptor[] events;
    private int defaultEvent;
    private PropertyDescriptor[] properties;
    private int defaultProperty;
    private MethodDescriptor[] methods;
    private BeanInfo targetBeanInfo;

    public GenericBeanInfo(BeanDescriptor beanDescriptor, EventSetDescriptor[] events, int defaultEvent,
            PropertyDescriptor[] properties, int defaultProperty, MethodDescriptor[] methods, BeanInfo targetBeanInfo) {
        this.beanDescriptor = beanDescriptor;
        this.events = events;
        this.defaultEvent = defaultEvent;
        this.properties = properties;
        this.defaultProperty = defaultProperty;
        this.methods = methods;
        this.targetBeanInfo = targetBeanInfo;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return properties;
    }

    @Override
    public int getDefaultPropertyIndex() {
        return defaultProperty;
    }

    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        return events;
    }

    @Override
    public int getDefaultEventIndex() {
        return defaultEvent;
    }

    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        return methods;
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return beanDescriptor;
    }

    @Override
    public java.awt.Image getIcon(int iconKind) {
        if (targetBeanInfo != null) {
            return targetBeanInfo.getIcon(iconKind);
        }
        return super.getIcon(iconKind);
    }
}
