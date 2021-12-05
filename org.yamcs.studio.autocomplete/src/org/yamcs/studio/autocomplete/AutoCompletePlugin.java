/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.yamcs.studio.autocomplete.sim.DSFunctionRegistry;
import org.yamcs.studio.autocomplete.sim.SimDSFunctionSet;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class AutoCompletePlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.autocomplete";
    public static final String HISTORY_TAG = "auto_complete_history";

    public static final Logger logger = Logger.getLogger(PLUGIN_ID);

    private static AutoCompletePlugin plugin;

    private static BundleContext bundleContext;

    private static Map<String, LinkedList<String>> fifos = null;
    private static IDialogSettings settings;

    private ImageRegistry imageRegistry;

    public static Logger getLogger() {
        return logger;
    }

    public static AutoCompletePlugin getDefault() {
        return plugin;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        DSFunctionRegistry.getDefault().registerDSFunctionSet(new SimDSFunctionSet());
        plugin = this;
        fifos = new HashMap<>();
        loadSettings();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        saveSettings();
        fifos.clear();
        fifos = null;
        plugin = null;
        super.stop(context);
    }

    /** Load persisted list values. */
    public synchronized void loadSettings() {
        if (plugin == null) {
            return;
        }
        IDialogSettings ds = plugin.getDialogSettings();
        if (ds != null) {
            settings = ds.getSection(HISTORY_TAG);
            if (settings == null) {
                settings = ds.addNewSection(HISTORY_TAG);
            }
        }
    }

    /** Save list values to persistent storage. */
    public synchronized void saveSettings() {
        IDialogSettings ds = plugin.getDialogSettings();
        if (ds != null) {
            for (Entry<String, LinkedList<String>> entry : fifos.entrySet()) {
                final String value_tag = entry.getKey();
                final LinkedList<String> fifo = entry.getValue();
                if (fifo != null && !fifo.isEmpty()) {
                    settings.put(value_tag,
                            fifo.toArray(new String[fifo.size()]));
                }
            }
        }
    }

    /** Clear list values from persistent storage. */
    public synchronized void clearSettings() {
        IDialogSettings ds = plugin.getDialogSettings();
        if (ds != null) {
            settings = ds.addNewSection(HISTORY_TAG);
        }
        fifos.clear();
    }

    public synchronized LinkedList<String> getHistory(final String type) {
        if (fifos.get(type) == null) {
            final LinkedList<String> fifo = new LinkedList<>();
            if (settings != null) {
                String values[] = settings.getArray(type);
                if (values != null) {
                    for (int i = values.length - 1; i >= 0; i--) {
                        // Load as if they were entered, i.e. skip duplicates
                        fifo.addFirst(values[i]);
                    }
                }
            }
            fifos.put(type, fifo);
        }
        return fifos.get(type);
    }

    /**
     * Load the <code>Image</code> from the given path in the given plugin.
     *
     * @param pluginId
     *            The id of the plugin that contains the requested image.
     * @param relativePath
     *            The resource path of the requested image.
     * @return The <code>Image</code> from the given path in the given plugin.
     */
    public Image getImageFromPlugin(final String pluginId,
            final String relativePath) {
        if (imageRegistry == null) {
            imageRegistry = new ImageRegistry(Display.getDefault());
        }
        String key = pluginId + "." + relativePath;
        // does image exist
        if (imageRegistry.get(key) == null) {
            ImageDescriptor descr = AbstractUIPlugin.imageDescriptorFromPlugin(
                    pluginId, relativePath);
            imageRegistry.put(key, descr);
        }
        return imageRegistry.get(key);
    }
}
