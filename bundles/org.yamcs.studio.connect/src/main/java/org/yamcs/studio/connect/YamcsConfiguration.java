package org.yamcs.studio.connect;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.equinox.security.storage.provider.IProviderHints;

/**
 * UI class. Used to maintain state of a server in the connection manager dialog
 */
public class YamcsConfiguration {

    public enum AuthType {
        STANDARD,
        KERBEROS;
    }

    private String id = UUID.randomUUID().toString(); // Used for linking a password from secure storage

    private String url;
    private String instance;
    private String user;
    private String name;

    private transient String transientPassword;

    private boolean savePassword;
    private String caCertFile;

    private AuthType authType;

    // Deprecated options are kept around for a long time to allow most users the time
    // to upgrade any local state (this object is mapped via gson).
    private @Deprecated String password;
    private @Deprecated String primaryHost;
    private @Deprecated Integer primaryPort;
    private @Deprecated boolean ssl;

    public void init() {
        // Temporary migration: host/port/ssl was moved into a single URL setting
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

        // Temporary migration: move password to secure storage
        if (password != null) {
            try {
                ISecurePreferences node = getSecureNode();
                node.put(id, password, true);
                node.flush();
            } catch (StorageException | IOException e) {
                throw new RuntimeException(e);
            }
            password = null;
        }

        // Our application will use the transient password. It is transient
        // so that gson ignores it when serializing to disk.
        transientPassword = getPasswordFromSecureStorage();
    }

    public String getId() {
        return id;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getComment() {
        return name;
    }

    public void setComment(String comment) {
        this.name = comment;
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

    public void setTransientPassword(String password) {
        this.transientPassword = password;
    }

    public String getTransientPassword() {
        return transientPassword;
    }

    private String getPasswordFromSecureStorage() {
        try {
            ISecurePreferences node = getSecureNode();
            return node.get(id, null);
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void savePassword() {
        try {
            ISecurePreferences node = getSecureNode();
            node.put(id, transientPassword, true);
            node.flush();
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ISecurePreferences getSecureNode() throws IOException {
        // Disable the default behaviour of showing an
        // (annoying) dialog inviting the user to set up
        // a master password recovery hint.
        Map<String, Object> options = new HashMap<>();
        options.put(IProviderHints.PROMPT_USER, false);

        // Use Eclipse default location, then it also shows in preference dialog:
        // ~/.eclipse/org.eclipse.equinox.security/secure_storage
        ISecurePreferences preferences = SecurePreferencesFactory.open(null, options);
        return preferences.node("org.yamcs.connect/passwords");
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
            ssl = "https".equals(uri.getScheme());
            if (primaryPort == -1) {
                primaryPort = ssl ? 443 : 80;
            }
        } catch (URISyntaxException e) {
            // Ignore
        }
    }

    public boolean isSavePassword() {
        return savePassword;
    }

    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    @Override
    public String toString() {
        return getURL();
    }
}
