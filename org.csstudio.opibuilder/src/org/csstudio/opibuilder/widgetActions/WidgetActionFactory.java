/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The factory creating all the widget actions.
 * 
 * @author Xihui Chen
 *
 */
public class WidgetActionFactory {
    public enum ActionType {
        OPEN_DISPLAY("Open OPI", createImage("icons/OPIRunner.png")),
        WRITE_PV("Write PV", createImage("icons/writePV.png")),
        EXECUTE_CMD("Execute Command", createImage("icons/command.gif")),
        EXECUTE_JAVASCRIPT("Execute Javascript", createImage("icons/exeJS.png")),
        EXECUTE_PYTHONSCRIPT("Execute Python Script", createImage("icons/exePy.gif")),
        PLAY_SOUND("Play WAV File", createImage("icons/sound.gif")),
        OPEN_FILE("Open File", createImage("icons/openFile.png")),
        OPEN_WEBPAGE("Open Webpage", createImage("icons/hyperlink.gif"));

        private ImageDescriptor iconImage;
        private String description;

        private ActionType(String description,
                ImageDescriptor iconImage) {
            this.description = description;
            this.iconImage = iconImage;
        }

        /**
         * Parse a string to an ActionType. The string should be equal to the results of element.toString().
         * 
         * @param actionString.
         * @return the ActionType. null if parse failed.
         */
        public static ActionType parseAction(String actionString) {
            // Map legacy actions
            if ("OPEN_OPI_IN_VIEW".equals(actionString)) {
                return OPEN_DISPLAY;
            }
            for (ActionType type : values()) {
                if (actionString.equals(type.toString())) {
                    return type;
                }
            }
            return null;
        }

        /**
         * @return the iconImageData
         */
        public ImageDescriptor getIconImage() {
            return iconImage;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        private static ImageDescriptor createImage(String path) {
            ImageDescriptor image = CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(
                    OPIBuilderPlugin.PLUGIN_ID, path);
            return image;
        }

    }

    public static AbstractWidgetAction createWidgetAction(ActionType actionType) {
        Assert.isNotNull(actionType);
        switch (actionType) {
        case OPEN_DISPLAY:
            return new OpenDisplayAction();
        case WRITE_PV:
            return new WritePVAction();
        case OPEN_FILE:
            return new OpenFileAction();
        case EXECUTE_CMD:
            return new ExecuteCommandAction();
        case EXECUTE_JAVASCRIPT:
            return new ExecuteJavaScriptAction();
        case EXECUTE_PYTHONSCRIPT:
            return new ExecutePythonScriptAction();
        case OPEN_WEBPAGE:
            return new OpenWebpageAction();
        case PLAY_SOUND:
            return new PlayWavFileAction();
        default:
            break;
        }
        return null;
    }
}
