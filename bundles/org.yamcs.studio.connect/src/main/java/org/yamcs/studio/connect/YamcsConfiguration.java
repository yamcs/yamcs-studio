package org.yamcs.studio.connect;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

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

    private String url;

    @Deprecated
    private String primaryHost;
    @Deprecated
    private Integer primaryPort;
    @Deprecated
    private boolean ssl;

    private boolean savePassword;
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
        return getUser() == null;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;

        // Temp to avoid migration issues with
        // Different clients making use of same connection settings
        try {
            URI uri = new URI(url);
            primaryHost = uri.getHost();
            primaryPort = uri.getPort();
            ssl = uri.getScheme().equals("https");
            if (primaryPort == -1) {
                primaryPort = ssl ? 443 : 80;
            }
        } catch (URISyntaxException e) {
            // Ignore
        }
    }

    @Deprecated
    public String getPrimaryHost() {
        return primaryHost;
    }

    @Deprecated
    public void setPrimaryHost(String primaryHost) {
        this.primaryHost = primaryHost;
    }

    @Deprecated
    public Integer getPrimaryPort() {
        return primaryPort;
    }

    @Deprecated
    public void setPrimaryPort(Integer primaryPort) {
        this.primaryPort = primaryPort;
    }

    public boolean isSavePassword() {
        return savePassword;
    }

    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    @Deprecated
    public boolean isSsl() {
        return ssl;
    }

    @Deprecated
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
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

    @Override
    public String toString() {
        return getURL();
    }

    public void upgrade() {
        if (url == null) {
            if (ssl) {
                if (primaryPort == 443) {
                    url = "https://" + primaryHost;
                } else {
                    url = "https://" + primaryHost + ":" + primaryPort;
                }
            } else {
                if (primaryPort == 80) {
                    url = "http://" + primaryHost;
                } else {
                    url = "http://" + primaryHost + ":" + primaryPort;
                }
            }
        }
    }
}
