package org.yamcs.studio.core.security;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.YamcsManagement.UserInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.client.YamcsClient;

import com.google.protobuf.InvalidProtocolBufferException;

public class YamcsAuthorizations {

    private static final Logger log = Logger.getLogger(YamcsAuthorizations.class.getName());

    public enum SystemPrivilege {
        MayControlYProcessor, MayModifyCommandHistory, MayControlCommandQueue, MayCommandPayload, MayGetMissionDatabase, MayControlArchiving
    }

    private static YamcsAuthorizations instance = new YamcsAuthorizations();
    private UserInfo userInfo;

    public static YamcsAuthorizations getInstance() {
        return instance;
    }

    public CompletableFuture<byte[]> loadAuthorizations() {
        YamcsClient yamcsClient = ConnectionManager.getInstance().getYamcsClient();
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

    public boolean hasSystemPrivilege(SystemPrivilege systemPrivilege) {
        if (!isAuthorizationEnabled()) {
            return true;
        }
        if (userInfo == null) {
            return false;
        }
        return userInfo.getSystemPrivilegesList().contains(systemPrivilege.name());
    }

    private boolean isAuthorizationEnabled() {
        return ConnectionManager.getInstance().isPrivilegesEnabled();
    }
}
