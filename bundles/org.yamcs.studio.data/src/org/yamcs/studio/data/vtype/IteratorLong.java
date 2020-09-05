package org.yamcs.studio.data.vtype;

/**
 * An iterator of {@code long}s.
 */
public abstract class IteratorLong implements IteratorNumber {

    @Override
    public float nextFloat() {
        return (float) nextLong();
    }

    @Override
    public double nextDouble() {
        return (double) nextLong();
    }

    @Override
    public byte nextByte() {
        return (byte) nextLong();
    }

    @Override
    public short nextShort() {
        return (short) nextLong();
    }

    @Override
    public int nextInt() {
        return (int) nextLong();
    }
}
