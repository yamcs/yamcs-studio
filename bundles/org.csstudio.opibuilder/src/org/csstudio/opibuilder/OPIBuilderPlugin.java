/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder;

import java.util.logging.Logger;

import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.script.ScriptService;
import org.csstudio.opibuilder.util.GUIRefreshThread;
import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.util.SchemaService;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Xihui Chen
 *
 */
@SuppressWarnings("deprecation")
public class OPIBuilderPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.csstudio.opibuilder";

    /**
     * The ID of the widget extension point.
     */
    public static final String EXTPOINT_WIDGET = PLUGIN_ID + ".widget";

    /**
     * The ID of the widget extension point.
     */
    public static final String EXTPOINT_FEEDBACK_FACTORY = PLUGIN_ID + ".graphicalFeedbackFactory";

    public static final String OPI_FILE_EXTENSION = "opi";

    private static final Logger logger = Logger.getLogger(PLUGIN_ID);

    private static OPIBuilderPlugin plugin;

    private IPropertyChangeListener preferenceListener;

    public OPIBuilderPlugin() {
        plugin = this;
    }

    public static OPIBuilderPlugin getDefault() {
        return plugin;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        // set this to resolve Xincludes in XMLs
        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");

        ScriptService.getInstance();

        preferenceListener = event -> {
            if (event.getProperty().equals(PreferencesHelper.COLOR_FILE)) {
                MediaService.getInstance().reloadColorFile();
            } else if (event.getProperty().equals(PreferencesHelper.FONT_FILE)) {
                MediaService.getInstance().reloadFontFile();
            } else if (event.getProperty().equals(PreferencesHelper.OPI_GUI_REFRESH_CYCLE)) {
                GUIRefreshThread.getInstance(true).reLoadGUIRefreshCycle();
            } else if (event.getProperty().equals(PreferencesHelper.SCHEMA_OPI)) {
                SchemaService.getInstance().reLoad();
            }
        };

        getPluginPreferences().addPropertyChangeListener(preferenceListener);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        getPluginPreferences().removePropertyChangeListener(preferenceListener);
    }

    public static Logger getLogger() {
        return logger;
    }
}
