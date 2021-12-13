/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

/**
 * An iterator of {@code byte}s.
 */
public abstract class IteratorByte implements IteratorNumber {

    @Override
    public float nextFloat() {
        return nextByte();
    }

    @Override
    public double nextDouble() {
        return nextByte();
    }

    @Override
    public short nextShort() {
        return nextByte();
    }

    @Override
    public int nextInt() {
        return nextByte();
    }

    @Override
    public long nextLong() {
        return nextByte();
    }
}
