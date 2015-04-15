package org.csstudio.platform.libs.yamcs.web;

import org.yamcs.protobuf.Rest.RestExceptionMessage;

import com.google.protobuf.MessageLite;

public interface ResponseHandler {

    /**
     * The (succesful) response of the server.
     */
    void onMessage(MessageLite responseMsg);

    /**
     * The (controlled exception) response of the server
     */
    void onException(RestExceptionMessage exceptionMsg);

    /**
     * When some uncaught exception occurred
     */
    void onFault(Throwable t);
}
