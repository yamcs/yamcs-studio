package org.csstudio.platform.libs.yamcs.web;

import org.yamcs.xtce.XtceDb;


/**
 * @param <T> TODO should use MessageHandler intead, but XtceDb does not
 * come from proto.
 */
@Deprecated
public interface XtceDbHandler {

    void onMessage(XtceDb msg);
    void onException(Throwable t);    
}
