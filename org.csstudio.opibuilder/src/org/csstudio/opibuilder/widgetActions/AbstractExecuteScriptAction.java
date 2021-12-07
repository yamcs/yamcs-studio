/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.FilePathProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.runtime.IPath;

/**
 * The abstract action for executing script.
 */
public abstract class AbstractExecuteScriptAction extends AbstractWidgetAction {

    public static final String PROP_PATH = "path";
    public static final String PROP_EMBEDDED = "embedded";
    public static final String PROP_SCRIPT_TEXT = "scriptText";

    private BufferedReader reader = null;
    private InputStream inputStream = null;

    @Override
    protected void configureProperties() {
        addProperty(new FilePathProperty(PROP_PATH, "File Path", WidgetPropertyCategory.Basic, "",
                new String[] { getFileExtension() }, false));
        addProperty(new StringProperty(PROP_SCRIPT_TEXT, "Script Text", WidgetPropertyCategory.Basic, getScriptHeader(),
                true, true));
        var embeddedProperty = new BooleanProperty(PROP_EMBEDDED, "Embedded", WidgetPropertyCategory.Basic, false);
        embeddedProperty.addPropertyChangeListener(evt -> {
            getProperty(PROP_PATH).setVisibleInPropSheet(!((Boolean) evt.getNewValue()));
            getProperty(PROP_SCRIPT_TEXT).setVisibleInPropSheet(((Boolean) evt.getNewValue()));
        });
        addProperty(embeddedProperty);
        getProperty(PROP_SCRIPT_TEXT).setVisibleInPropSheet(false);

    }

    protected IPath getPath() {
        return (IPath) getPropertyValue(PROP_PATH);
    }

    protected IPath getAbsolutePath() {
        // read file
        var absolutePath = getPath();
        if (!absolutePath.isAbsolute()) {
            absolutePath = ResourceUtil.buildAbsolutePath(getWidgetModel(), getPath());
        }
        return absolutePath;
    }

    protected boolean isEmbedded() {
        return (Boolean) getPropertyValue(PROP_EMBEDDED);
    }

    protected String getScriptText() {
        return (String) getPropertyValue(PROP_SCRIPT_TEXT);
    }

    @Override
    public String getDefaultDescription() {
        var desc = super.getDefaultDescription();
        if (isEmbedded()) {
            return desc;
        }
        return desc + " " + getPath();
    }

    /**
     * Get reader of the script file.An instance will be created for later to use. Muse call {@link #closeReader()} to
     * close this reader.
     * 
     * @return the reader
     * @throws Exception
     */
    protected BufferedReader getReader() throws Exception {
        if (reader == null) {
            inputStream = ResourceUtil.pathToInputStream(getAbsolutePath());
            reader = new BufferedReader(new InputStreamReader(inputStream));
        }
        return reader;
    }

    protected void closeReader() {
        if (reader != null) {
            try {
                inputStream.close();
                reader.close();
            } catch (IOException e) {
            }
            inputStream = null;
            reader = null;
        }
    }

    /**
     * Get raw InputStream of the script file. Make sure to call close() of the returned instance.
     * 
     * @return InputStream of the script file.
     * @throws Exception
     */
    protected InputStream getInputStream() throws Exception {
        return ResourceUtil.pathToInputStream(getAbsolutePath());
    }

    protected abstract String getFileExtension();

    protected abstract String getScriptHeader();
}
