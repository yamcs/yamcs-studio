package org.yamcs.studio.core.security;

import java.util.concurrent.CompletableFuture;

import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.UserInfo;
import org.yamcs.studio.core.YamcsPlugin;

public class YamcsAuthorizations {

    public static final String Command = "Command";

    private static YamcsAuthorizations instance = new YamcsAuthorizations();
    private UserInfo userInfo;

    public static YamcsAuthorizations getInstance() {
        return instance;
    }

    public CompletableFuture<UserInfo> loadAuthorizations() {
        YamcsClient client = YamcsPlugin.getYamcsClient();
        return client.getOwnUserInfo().whenComplete((info, exc) -> {
            if (exc == null) {
                this.userInfo = info;
            }
        });
    }

    public String getUsername() {
        if (userInfo != null) {
            return userInfo.getName();
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
        return false;
        /*YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        // TODO we should probably control this from the server, rather than here. Just because
        // the creds are null, does not really mean anything. We could also send creds to an
        // unsecured yamcs server. It would just ignore it, and then our client state would
        // be wrong
        YamcsConnectionProperties yprops = yamcsClient.getYamcsConnectionProperties();
        return (yprops == null) ? false : yprops.getPassword() != null;*/
    }
}
