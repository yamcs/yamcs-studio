package org.yamcs.studio.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Rest.RestListAuthorisationsResponse;
import org.yamcs.protobuf.Rest.UserAuthorizationsInfo;
import org.yamcs.studio.core.web.ResponseHandler;

import com.google.protobuf.MessageLite;

public class YamcsAuthorizations {

    private static final Logger log = Logger.getLogger(YamcsLoginModule.class.getName());

    public enum SystemPrivilege {
        MayControlYProcessor,
        MayModifyCommandHistory,
        MayControlCommandQueue,
        MayCommandPayload,
        MayGetMissionDatabase,
        MayControlArchiving
    }

    private static YamcsAuthorizations instance = new YamcsAuthorizations();
    private UserAuthorizationsInfo userAuthorizationsInfo;

    public static YamcsAuthorizations getInstance()
    {
        return instance;
    }

    public void getAuthorizations()
    {
        ConnectionManager.getInstance().getRestClient().listAuthorizations(new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                Display.getDefault().asyncExec(() -> {
                    RestListAuthorisationsResponse response = (RestListAuthorisationsResponse) responseMsg;
                    userAuthorizationsInfo = response.getUserAuthorizationsInfo();
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not get authorizations list", e);
            }
        });
    }

    public boolean hasSystemPrivilege(SystemPrivilege systemPrivilege)
    {
        if (!isAuthorizationEnabled())
            return true;
        if (userAuthorizationsInfo == null)
            return false;
        return userAuthorizationsInfo.getSystemPrivilegesList().contains(systemPrivilege.name());
    }

    private boolean isAuthorizationEnabled()
    {
        return ConnectionManager.getInstance().isPrivilegesEnabled();
    }

}
