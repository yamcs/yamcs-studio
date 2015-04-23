package org.yamcs.studio.client;

import org.yamcs.protobuf.Yamcs.NamedObjectId;

/**
 * When a channel name was deemed invalid by the yamcs server
 */
public class InvalidIdentification extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidIdentification(NamedObjectId id) {
        super("Invalid channel name: \"" + id.getName() + "\"");
    }
}
