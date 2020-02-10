package org.yamcs.studio.core.ui.connections;

import java.util.Objects;

import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.api.YamcsConnectionProperties.Protocol;

/**
 * UI class. Used to maintain state of a server in the connection manager dialog
 */
public class YamcsConfiguration {

    public enum AuthType {
        STANDARD,
        KERBEROS;
    }

    private String name;
    private String instance;
    private String user;
    private String password;

    private String primaryHost;
    private Integer primaryPort;

    private boolean savePassword;
    private boolean ssl;
    private String caCertFile;

    private AuthType authType;

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setCaCertFile(String caCertFile) {
        this.caCertFile = caCertFile;
    }

    public String getCaCertFile() {
        return caCertFile;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public boolean isAnonymous() {
        return authType == AuthType.STANDARD
                && getUser() == null;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getPrimaryHost() {
        return primaryHost;
    }

    public void setPrimaryHost(String primaryHost) {
        this.primaryHost = primaryHost;
    }

    public Integer getPrimaryPort() {
        return primaryPort;
    }

    public void setPrimaryPort(Integer primaryPort) {
        this.primaryPort = primaryPort;
    }

    public boolean isSavePassword() {
        return savePassword;
    }

    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getConnectionString() {
        if (instance == null || "".equals(instance)) {
            return "yamcs://" + primaryHost + ":" + primaryPort;
        } else {
            return "yamcs://" + primaryHost + ":" + primaryPort + "/" + instance;
        }
    }

    public YamcsConnectionProperties getConnectionProperties() {
        YamcsConnectionProperties yprops = new YamcsConnectionProperties(primaryHost, primaryPort, instance);
        yprops.setProtocol(Protocol.http);
        yprops.setTls(ssl);
        if (authType == AuthType.KERBEROS) {
            yprops.setAuthType(org.yamcs.api.YamcsConnectionProperties.AuthType.KERBEROS);
        } else {
            yprops.setAuthType(org.yamcs.api.YamcsConnectionProperties.AuthType.STANDARD);
            if (!isAnonymous() && password != null) {
                yprops.setCredentials(user, password.toCharArray());
            }
        }
        return yprops;
    }

    @Override
    public boolean equals(Object obj) {
        // TODO This is not exactly true, since we don't currently check for
        // duplicates.
        // We do need an equals-method though, as it is used to compare the
        // last-used configuration
        // with the list of all configurations.
        if (obj == null) {
            return false;
        }
        YamcsConfiguration other = (YamcsConfiguration) obj;
        return Objects.equals(name, other.name);
    }
}
