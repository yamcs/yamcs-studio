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
