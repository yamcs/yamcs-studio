/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding;

import java.io.File;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CommandingPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.commanding";

    private static final Logger log = Logger.getLogger(CommandingPlugin.class.getName());

    private static CommandingPlugin plugin;

    private String lastStackFolder;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    public static CommandingPlugin getDefault() {
        return plugin;
    }

    public ImageDescriptor getImageDescriptor(String path) {
        return ImageDescriptor.createFromURL(getBundle().getEntry(path));
    }

    public IDialogSettings getCommandHistoryTableSettings() {
        var settings = getDialogSettings();
        var section = settings.getSection("cmdhist-table");
        if (section == null) {
            section = settings.addNewSection("cmdhist-table");
        }
        return section;
    }

    public String getPreferredNamespace() {
        var store = getPreferenceStore();
        var preferredNamespace = store.getString(CommandingPreferencePage.PREF_PREFERRED_NAMESPACE);
        return preferredNamespace.isEmpty() ? null : preferredNamespace;
    }

    public int getDefaultStackWait() {
        var store = getPreferenceStore();
        return store.getInt(CommandingPreferencePage.PREF_DEFAULT_STACK_WAIT);
    }

    /**
     * Returns the folder where stack import/export dialogs should open: the last used folder of this session, or else
     * the configured stack folder. Returns null if neither is an existing directory.
     */
    public String getStackFolder() {
        if (lastStackFolder != null && new File(lastStackFolder).isDirectory()) {
            return lastStackFolder;
        }
        var folder = getPreferenceStore().getString(CommandingPreferencePage.PREF_STACK_FOLDER).trim();
        if (folder.isEmpty()) {
            return null;
        }
        folder = expandHome(folder);
        if (!new File(folder).isDirectory()) {
            log.warning("Configured command stack folder is not an existing directory: " + folder);
            return null;
        }
        return folder;
    }

    /**
     * Remembers the folder of the given file for the remainder of this session.
     */
    public void setStackFolder(String file) {
        lastStackFolder = new File(file).getParent();
    }

    public void resetStackFolder() {
        lastStackFolder = null;
    }

    /**
     * Replaces a leading {@code ~} with the user's home directory.
     */
    public static String expandHome(String path) {
        if (path.equals("~") || path.startsWith("~/") || path.startsWith("~" + File.separator)) {
            return System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }
}
