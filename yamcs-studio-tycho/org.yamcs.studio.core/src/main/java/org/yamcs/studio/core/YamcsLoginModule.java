package org.yamcs.studio.core;

import java.security.Principal;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Rest.RestListAvailableParametersRequest;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

import com.google.protobuf.MessageLite;

public class YamcsLoginModule implements LoginModule {

    private static final Logger log = Logger.getLogger(YamcsLoginModule.class.getName());

    private Subject subject;
    private CallbackHandler callbackHandler;

    /** Name of authenticated user or <code>null</code> */
    private String user = null;
    private char[] password = null;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;

    }

    @Override
    public boolean login() throws LoginException {

        if (callbackHandler == null)
            throw new LoginException("No CallbackHandler");

        final String user_pw[] = getUserPassword();
        if (authenticate(user_pw[0], user_pw[1])) {
            user = user_pw[0];
            password = user_pw[1].toCharArray();
            return true;
        } else {
            throw new LoginException("Invalid user or password. Please try again");
        }
    }

    /**
     * Obtain user name and password via callbacks
     *
     * @return Array with user name and password
     * @throws LoginException
     *             on error
     */
    private String[] getUserPassword() throws LoginException {
        final NameCallback name = new NameCallback("User Name:");
        final PasswordCallback password = new PasswordCallback("Password :", false);
        try {
            callbackHandler.handle(new Callback[] { name, password });
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new LoginException("Cannot get user/password");
        }
        final String result[] = new String[] {
                name.getName(),
                new String(password.getPassword())
        };
        password.clearPassword();
        return result;
    }

    /**
     * authenticate() This is a method to test if authorization is allowed via the REST Api. It
     * could be attempted on any service of the API, the actual data returned is not used.
     */
    private boolean authenticate(final String user, final String password) {
        log.info("yamcs login, authenticating " + user + "/****");

        RestListAvailableParametersRequest.Builder req = RestListAvailableParametersRequest.newBuilder();

        YamcsConnectionProperties webProps = ConnectionManager.getInstance().getWebProperties();
        RestClient restClient = new RestClient(webProps, new YamcsCredentials(user, password));

        AuthReponseHandler arh = new AuthReponseHandler();

        try {
            restClient.listAvailableParameters(req.build(), arh);
        } catch (Exception e) {
            return false;
        }

        while (!arh.resultReceived) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return arh.authenticated;
    }

    @Override
    public boolean commit() throws LoginException {
        log.info("yamcs login, commit");
        if (user == null)
            return false;
        Principal principal = new SimplePrincipal(user);
        subject.getPrincipals().add(principal);

        ConnectionManager connectionManager = ConnectionManager.getInstance();
        connectionManager.connectWithNewCredentials(new YamcsCredentials(user, password));
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return true;
    }

    /**
     * never called by the org.csstudio.security plugin. So has been replaced by a new menu/handler
     * in org.yamcs.studio.ui
     */
    @Override
    public boolean logout() throws LoginException {
        log.info("yamcs login, logout");
        try {
            YamcsPlugin.getDefault().getConnectionManager().disconnect();
        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }
        return true;
    }

    private class AuthReponseHandler implements ResponseHandler {

        public boolean resultReceived = false;
        public boolean authenticated = false;

        @Override
        public void onMessage(MessageLite responseMsg) {
            resultReceived = true;
            authenticated = true;
        }

        @Override
        public void onException(Exception e) {
            log.log(Level.WARNING, "Could not authenticate", e);

            resultReceived = true;
            authenticated = false;
        }

    }
}
