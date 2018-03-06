/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * Basic type definition for all scalar types. {@link #getValue()} never returns
 * null. One <b>must always look</b>
 * at the alarm severity to be able to correctly interpret the value.
 * <p>
 * As of 1.1, this class is not a generic type. This is due to a bug in 1.6
 * compiler where generic return type clash with covariant return types.
 *
 * @author carcassi
 */
public abstract class Scalar extends VType implements AlarmProvider, TimeProvider {

    /**
     * Returns the value. Never null.
     *
     * @return the value
     */
    public abstract Object getValue();
}
