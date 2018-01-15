/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

/**
 * Represent a building block that can store a particular value
 *
 * @param <T> the type of the value held by the cache
 * @author carcassi
 */
public interface ValueCache<T> extends ReadFunction<T>, WriteFunction<T> {

    /**
     * The type of objects that this cache can contain.
     *
     * @return the class token
     */
    public Class<T> getType();

}
