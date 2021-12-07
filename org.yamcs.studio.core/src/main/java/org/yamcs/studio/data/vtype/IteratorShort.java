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

public abstract class IteratorShort implements IteratorNumber {

    @Override
    public float nextFloat() {
        return (float) nextShort();
    }

    @Override
    public double nextDouble() {
        return (double) nextShort();
    }

    @Override
    public byte nextByte() {
        return (byte) nextShort();
    }

    @Override
    public int nextInt() {
        return (int) nextShort();
    }

    @Override
    public long nextLong() {
        return (long) nextShort();
    }

}
