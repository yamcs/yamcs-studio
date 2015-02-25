package org.csstudio.platform.libs.yamcs.web;

import io.protostuff.Message;

public interface MessageHandler<T extends Message<T>> {

    void onMessage(T msg);
    void onException(Throwable t);    
}
