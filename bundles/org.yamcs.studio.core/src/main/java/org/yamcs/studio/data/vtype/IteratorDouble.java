package org.yamcs.studio.data.vtype;

/**
 * An iterator of {@code double}s.
 */
public abstract class IteratorDouble implements IteratorNumber {

    @Override
    public float nextFloat() {
        return (float) nextDouble();
    }

    @Override
    public byte nextByte() {
        return (byte) nextDouble();
    }

    @Override
    public short nextShort() {
        return (short) nextDouble();
    }

    @Override
    public int nextInt() {
        return (int) nextDouble();
    }

    @Override
    public long nextLong() {
        return (long) nextDouble();
    }
}
