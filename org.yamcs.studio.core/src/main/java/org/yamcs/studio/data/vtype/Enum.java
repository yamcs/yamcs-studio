package org.yamcs.studio.data.vtype;

import java.util.List;

/**
 * Metadata for enumerations.
 */
public interface Enum {

    /**
     * All the possible labels. Never null.
     *
     * @return the possible values
     */
    List<String> getLabels();
}
