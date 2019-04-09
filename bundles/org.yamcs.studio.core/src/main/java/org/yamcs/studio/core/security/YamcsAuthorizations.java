package org.yamcs.studio.core.security;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.UserInfo;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsStudioClient;

import com.google.protobuf.InvalidProtocolBufferException;

public class YamcsAuthorizations {

    private static final Logger log = Logger.getLogger(YamcsAuthorizations.class.getName());

    public static final String ControlCommandQueue = "ControlCommandQueue";
    public static final String Command = "Command";

    private static YamcsAuthorizations instance = new YamcsAuthorizations();
    private UserInfo userInfo;

    public static YamcsAuthorizations getInstance() {
        return instance;
    }

    public CompletableFuture<byte[]> loadAuthorizations() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.get("/user", null).whenComplete((data, exc) -> {
            if (exc == null) {
                try {
                    userInfo = UserInfo.parseFrom(data);
                } catch (InvalidProtocolBufferException e) {
                    log.log(Level.SEVERE, "Failed to decode server message", e);
                }
            }
        });
    }

    public String getUsername() {
        if (userInfo != null) {
            return userInfo.getLogin();
        }
        return null;
    }

    private boolean isSuperuser() {
        return userInfo != null && (userInfo.hasSuperuser() && userInfo.getSuperuser());
    }

    public boolean hasSystemPrivilege(String systemPrivilege) {
        if (!isAuthorizationEnabled()) {
            return true;
        }
        if (userInfo == null) {
            return false;
        }

        return isSuperuser() || userInfo.getSystemPrivilegeList().contains(systemPrivilege);
    }

    public boolean isAuthorizationEnabled() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        // TODO we should probably control this from the server, rather than here. Just because
        // the creds are null, does not really mean anything. We could also send creds to an
        // unsecured yamcs server. It would just ignore it, and then our client state would
        // be wrong
        YamcsConnectionProperties yprops = yamcsClient.getYamcsConnectionProperties();
        return (yprops == null) ? false : yprops.getPassword() != null;
    }
}
