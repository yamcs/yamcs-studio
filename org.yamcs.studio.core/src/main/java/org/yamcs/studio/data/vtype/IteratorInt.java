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
 * An iterator of {@code int}s.
 */
public abstract class IteratorInt implements IteratorNumber {

    @Override
    public float nextFloat() {
        return (float) nextInt();
    }

    @Override
    public double nextDouble() {
        return (double) nextInt();
    }

    @Override
    public byte nextByte() {
        return (byte) nextInt();
    }

    @Override
    public short nextShort() {
        return (short) nextInt();
    }

    @Override
    public long nextLong() {
        return (long) nextInt();
    }
}
