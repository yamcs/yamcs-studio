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
