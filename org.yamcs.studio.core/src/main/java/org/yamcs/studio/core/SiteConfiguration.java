/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.util.Util;

public class SiteConfiguration {

    private static Properties props = new Properties();

    static {
        // Read site-specific configuration, this can be used to toggle some functionality
        // in a site-controlled manner.
        var configurationDir = findInstallationConfigurationDir();
        if (configurationDir != null) {
            var siteConfig = configurationDir.resolve("site-config.ini");
            if (Files.exists(siteConfig)) {
                try (var fileIn = Files.newInputStream(siteConfig)) {
                    props.load(fileIn);
                } catch (IOException e) {
                    System.err.println("Failed to load site configuration");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Search for the location of the 'configuration' dir, under the installation root.
     * <p>
     * Note that we cannot use something like {@code ConfigurationScope.INSTANCE.getLocation()} because it is not
     * reliable in package installations where the installation directory is readonly. In such cases Eclipse will return
     * a non-shared path under {@code ~/.eclipse/} which does not help is in locating any {@code site-config.ini} file.
     */
    private static Path findInstallationConfigurationDir() {
        Path installDir;
        try {
            installDir = Path.of(Platform.getInstallLocation().getURL().toURI());
        } catch (URISyntaxException e) {
            System.err.println("Failed to locate install directory");
            e.printStackTrace();
            return null;
        }

        // Location on Linux/Windows
        var configurationDir = installDir.resolve("configuration");
        if (Files.exists(configurationDir)) {
            return configurationDir;
        }

        // Location on macOS
        if (Util.isMac()) {
            var productName = Platform.getProduct().getName();
            configurationDir = installDir.resolve(productName + ".app").resolve("Contents/Eclipse/configuration");
            if (Files.exists(configurationDir)) {
                return configurationDir;
            }
        }

        // Fallback, maybe a developer build
        return ConfigurationScope.INSTANCE.getLocation().toFile().toPath();
    }

    public static Optional<Boolean> isSpellEnabled() {
        var spellEnabled = props.getProperty("spell.enabled");
        if (spellEnabled != null) {
            return Optional.of(Boolean.parseBoolean(spellEnabled));
        } else {
            return Optional.empty();
        }
    }
}
