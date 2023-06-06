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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.core.runtime.preferences.ConfigurationScope;

public class SiteConfiguration {

    private static Properties props = new Properties();

    static {
        // Read site-specific configuration, this can be used to toggle some functionality
        // in a site-controlled manner.
        var configLocation = ConfigurationScope.INSTANCE.getLocation().toFile();
        var configFile = new File(configLocation, "site-config.ini");
        var siteConfiguration = new Properties();
        if (configFile.exists()) {
            try (var fileIn = Files.newInputStream(configFile.toPath())) {
                siteConfiguration.load(fileIn);
            } catch (IOException e) {
                System.err.println("Failed to load site configuration");
                e.printStackTrace();
            }
        }
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
