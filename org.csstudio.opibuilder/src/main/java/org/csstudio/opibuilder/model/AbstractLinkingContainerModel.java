/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.model;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.FilePathProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.runmode.IOPIRuntime;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.GraphicalViewer;

/**
 * The abstract base model for LinkingContainer widgets.
 */
public abstract class AbstractLinkingContainerModel extends AbstractContainerModel {

    /**
     * The ID of the resource property.
     */
    public static final String PROP_OPI_FILE = "opi_file";

    /**
     * The name of the group container widget in the OPI file, which will be loaded if it is specified. If it is not
     * specified, the whole OPI file will be loaded.
     */
    public static final String PROP_GROUP_NAME = "group_name";

    /**
     * The display Scale options of the embedded OPI.
     */
    private DisplayModel displayModel = null;

    @Override
    protected void configureBaseProperties() {
        super.configureBaseProperties();

        addProperty(new FilePathProperty(PROP_OPI_FILE, "OPI File", WidgetPropertyCategory.Behavior, "",
                new String[] { "opi" }));

        addProperty(new StringProperty(PROP_GROUP_NAME, "Group Name", WidgetPropertyCategory.Behavior, ""));
    }

    public String getGroupName() {
        return (String) getPropertyValue(PROP_GROUP_NAME);
    }

    /**
     * Return the target resource.
     *
     * @return The target resource.
     */
    public IPath getOPIFilePath() {
        var absolutePath = Path.fromPortableString((String) getProperty(PROP_OPI_FILE).getPropertyValue());
        if (absolutePath != null && !absolutePath.isEmpty() && !absolutePath.isAbsolute()) {
            absolutePath = ResourceUtil.buildAbsolutePath(this, absolutePath);
        }
        return absolutePath;
    }

    public void setOPIFilePath(String path) {
        setPropertyValue(PROP_OPI_FILE, path);
    }

    /**
     * Set the display model of the loaded opi.
     *
     * @param displayModel
     */
    public synchronized void setDisplayModel(DisplayModel displayModel) {
        this.displayModel = displayModel;
    }

    /**
     * @return display model of the loaded opi. null if no opi has been loaded.
     */
    public synchronized DisplayModel getDisplayModel() {
        return displayModel;
    }

    public synchronized void setDisplayModelViewer(GraphicalViewer viewer) {
        displayModel.setViewer(viewer);
    }

    public synchronized void setDisplayModelDisplayID(int displayID) {
        displayModel.setDisplayID(displayID);
    }

    public synchronized void setDisplayModelExecutionMode(ExecutionMode executionMode) {
        displayModel.setExecutionMode(executionMode);
    }

    public synchronized void setDisplayModelOpiRuntime(IOPIRuntime opiRuntime) {
        displayModel.setOpiRuntime(opiRuntime);
    }
}
