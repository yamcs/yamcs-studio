package org.yamcs.studio.core.model;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public interface ExtensionCatalogue extends Catalogue {

    void processMessage(int extensionType, ByteString msg) throws InvalidProtocolBufferException;
}
