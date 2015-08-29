package org.yamcs.studio.core;

public class YamcsConnectionConfiguration {
    private String name;
    private String instance;
    private YamcsCredentials credentials;

    private String primaryHost;
    private Integer primaryPort;

    private String failoverHost;
    private Integer failoverPort;

    public YamcsConnectionConfiguration(String name, String host, int port, String instance) {
        this.name = name;
        this.instance = instance;
        primaryHost = host;
        primaryPort = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstance() {
        return instance;
    }

    public void setYamcsCredentials(YamcsCredentials credentials) {
        this.credentials = credentials;
    }

    public YamcsCredentials getYamcsCredentials() {
        return credentials;
    }

    public String getPrimaryHost() {
        return primaryHost;
    }

    public Integer getPrimaryPort() {
        return primaryPort;
    }

    public void setFailoverHost(String failoverHost) {
        this.failoverHost = failoverHost;
    }

    public String getFailoverHost() {
        return failoverHost;
    }

    public void setFailoverPort(int failoverPort) {
        this.failoverPort = failoverPort;
    }

    public Integer getFailoverPort() {
        return failoverPort;
    }

    @Deprecated
    public int getHornetQPort() {
        return 5445;
    }
}
