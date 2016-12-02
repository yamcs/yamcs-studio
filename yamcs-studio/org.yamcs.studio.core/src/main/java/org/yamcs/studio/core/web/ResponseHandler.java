package org.yamcs.studio.core.web;

import com.google.protobuf.MessageLite;

@Deprecated
public interface ResponseHandler {

    /**
     * The response of the server. Could be a RestExceptionMessage.
     */
    void onMessage(MessageLite responseMsg);

    /**
     * When some uncaught exception occurred
     */
    void onException(Exception e);
}
