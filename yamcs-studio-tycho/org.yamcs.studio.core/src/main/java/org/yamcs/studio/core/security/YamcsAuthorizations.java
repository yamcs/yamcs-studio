package org.yamcs.studio.core.security;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.YamcsManagement.UserInfo;
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
    private UserInfo userInfo;

    public static YamcsAuthorizations getInstance() {
        return instance;
    }

    public void loadAuthorizations() {

        ConnectionManager manager = ConnectionManager.getInstance();

        synchronized (manager) {
            manager.requestAuthenticatedUser(new ResponseHandler() {
                @Override
                public void onMessage(MessageLite responseMsg) {
                    synchronized (manager) {
                        userInfo = (UserInfo) responseMsg;
                        manager.notify();
                    }
                }

                @Override
                public void onException(Exception e) {

                    synchronized (manager) {
                        log.log(Level.SEVERE, "Could not get authorizations", e);
                        manager.notify();
                    }
                }
            });

            try {
                manager.wait();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
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
