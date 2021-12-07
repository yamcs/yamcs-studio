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
 * An iterator of {@code float}s.
 */
public abstract class IteratorFloat implements IteratorNumber {

    @Override
    public double nextDouble() {
        return (double) nextFloat();
    }

    @Override
    public byte nextByte() {
        return (byte) nextFloat();
    }

    @Override
    public short nextShort() {
        return (short) nextFloat();
    }

    @Override
    public int nextInt() {
        return (int) nextFloat();
    }

    @Override
    public long nextLong() {
        return (long) nextFloat();
    }
}
