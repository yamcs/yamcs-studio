package org.yamcs.studio.data.vtype;

/**
 * Scalar enum with alarm and timestamp. Given that enumerated values are of very limited use without the labels, and
 * that the current label is the data most likely used, the enum is of type {@link String}. The index is provided as an
 * extra field, and the list of all possible values is always provided.
 */
public interface VEnum extends Scalar, Enum, Alarm, Time, VType {

    @Override
    String getValue();

    /**
     * Return the index of the value in the list of labels.
     *
     * @return the current index
     */
    int getIndex();

}
