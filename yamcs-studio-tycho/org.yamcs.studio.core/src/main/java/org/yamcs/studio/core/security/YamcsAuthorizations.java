package org.yamcs.studio.core.security;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.Yamcs.ListAuthorizationsResponse;
import org.yamcs.protobuf.Yamcs.UserAuthorizationInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.web.ResponseHandler;

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
    private UserAuthorizationInfo userAuthorizationsInfo;

    public static YamcsAuthorizations getInstance() {
        return instance;
    }

    public void getAuthorizations() {
        ConnectionManager.getInstance().getRestClient().listAuthorizations(new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                ListAuthorizationsResponse response = (ListAuthorizationsResponse) responseMsg;
                userAuthorizationsInfo = response.getUserAuthorizationInfo();
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
        if (userAuthorizationsInfo == null)
            return false;
        return userAuthorizationsInfo.getSystemPrivilegesList().contains(systemPrivilege.name());
    }

    private boolean isAuthorizationEnabled() {
        return ConnectionManager.getInstance().isPrivilegesEnabled();
    }
}
