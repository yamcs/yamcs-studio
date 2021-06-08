package org.yamcs.studio.data.vtype;

/**
 * A collection of {@code long}s.
 */
public interface CollectionLong extends CollectionNumber {

    @Override
    IteratorLong iterator();
}
