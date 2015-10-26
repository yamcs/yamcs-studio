package org.yamcs.studio.core.security;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.Yamcs.UserInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

import com.google.protobuf.MessageLite;

public class YamcsAuthorizations {

    private static final Logger log = Logger.getLogger(YamcsAuthorizations.class.getName());

    public enum SystemPrivilege {
        MayControlYProcessor,
        MayModifyCommandHistory,
        MayControlCommandQueue,
        MayCommandPayload,
        MayGetMissionDatabase,
        MayControlArchiving
    }

    private static YamcsAuthorizations instance = new YamcsAuthorizations();
    private UserInfo userInfo;

    public static YamcsAuthorizations getInstance() {
        return instance;
    }

    public void loadAuthorizations() {
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        restClient.getAuthenticatedUser(new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                userInfo = (UserInfo) responseMsg;
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not get authorizations", e);
            }
        });
    }

    public boolean hasSystemPrivilege(SystemPrivilege systemPrivilege) {
        if (!isAuthorizationEnabled())
            return true;
        if (userInfo == null)
            return false;
        return userInfo.getSystemPrivilegesList().contains(systemPrivilege.name());
    }

    private boolean isAuthorizationEnabled() {
        return ConnectionManager.getInstance().isPrivilegesEnabled();
    }
}
