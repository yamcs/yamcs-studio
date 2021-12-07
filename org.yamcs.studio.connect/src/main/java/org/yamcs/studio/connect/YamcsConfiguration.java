/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.connect;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * UI class. Used to maintain state of a server in the connection manager dialog
 */
public class YamcsConfiguration {

    public enum AuthType {
        STANDARD, KERBEROS;
    }

    // Used for linking a password from secure storage
    private String id = UUID.randomUUID().toString();

    private String url;
    private String instance;
    private String user;
    private String name;

    private transient String transientPassword;

    private String caCertFile;

    private AuthType authType;

    // If false, don't even attempt to access secure storage
    // Thereby avoiding unnecessary prompts.
    private boolean secureHint;

    // Deprecated options are kept around for a long time to allow most users the time
    // to upgrade any local state (this object is mapped via gson).
    private @Deprecated String password;
    private @Deprecated String primaryHost;
    private @Deprecated Integer primaryPort;
    private @Deprecated boolean ssl;
    private @Deprecated @SuppressWarnings("unused") boolean savePassword;

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

        // Temporary migration: remove unsecure passwords
        if (password != null) {
            password = null;
        }
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

    public boolean isSecureHint() {
        return secureHint;
    }

    public void setSecureHint(boolean secureHint) {
        this.secureHint = secureHint;
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

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;

        // Temp to avoid migration issues with
        // Different clients making use of same connection settings
        try {
            var uri = new URI(url);
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

    @Override
    public String toString() {
        return getURL();
    }
}
