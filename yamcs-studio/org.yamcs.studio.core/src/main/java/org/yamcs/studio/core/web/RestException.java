package org.yamcs.studio.core.web;

import org.yamcs.protobuf.Web.RestExceptionMessage;

/**
 * Wraps a RestExceptionMessage in a java Exception for easier downstream consumption
 */
public class RestException extends Exception {
    private static final long serialVersionUID = 1L;
    private RestExceptionMessage restExceptionMessage;

    public RestException(RestExceptionMessage restExceptionMessage) {
        super(String.format("[%s] %s", restExceptionMessage.getType(), restExceptionMessage.getMsg()));
        this.restExceptionMessage = restExceptionMessage;
    }

    public RestExceptionMessage getRestExceptionMessage() {
        return restExceptionMessage;
    }
}
