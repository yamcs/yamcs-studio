package org.yamcs.studio.data.vtype;

/**
 * An iterator of {@code byte}s.
 */
public abstract class IteratorByte implements IteratorNumber {

    @Override
    public float nextFloat() {
        return (float) nextByte();
    }

    @Override
    public double nextDouble() {
        return (double) nextByte();
    }

    @Override
    public short nextShort() {
        return (short) nextByte();
    }

    @Override
    public int nextInt() {
        return (int) nextByte();
    }

    @Override
    public long nextLong() {
        return (long) nextByte();
    }
}
