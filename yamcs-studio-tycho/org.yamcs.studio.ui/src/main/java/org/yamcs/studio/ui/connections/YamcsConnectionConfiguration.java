package org.yamcs.studio.ui.connections;

import org.yamcs.studio.core.YamcsCredentials;

/**
 * UI class. Used to maintain state of a server in the connection manager dialog
 */
public class YamcsConnectionConfiguration {
    private String name;
    private String instance;
    private String user;
    private String password;

    private String primaryHost;
    private Integer primaryPort;

    private String failoverHost;
    private Integer failoverPort;

    private boolean savePassword;

    public YamcsCredentials getYamcsCredentials() {
        if (user != null && password != null)
            return new YamcsCredentials(user, password);
        return null;
    }

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

    public void setFailoverHost(String failoverHost) {
        this.failoverHost = failoverHost;
    }

    public String getFailoverHost() {
        return failoverHost;
    }

    public void setFailoverPort(Integer failoverPort) {
        this.failoverPort = failoverPort;
    }

    public Integer getFailoverPort() {
        return failoverPort;
    }

    public boolean isSavePassword() {
        return savePassword;
    }

    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    @Deprecated
    public int getHornetQPort() {
        return 5445;
    }
}
