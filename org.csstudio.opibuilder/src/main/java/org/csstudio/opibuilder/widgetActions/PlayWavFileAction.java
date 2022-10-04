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

import java.io.File;
import java.net.URL;
import java.util.logging.Level;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.properties.FilePathProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * An action which plays a .wav file.
 */
public class PlayWavFileAction extends AbstractWidgetAction {

    public static final String PROP_PATH = "path";

    @Override
    protected void configureProperties() {
        addProperty(new FilePathProperty(PROP_PATH, "WAV File Path", WidgetPropertyCategory.Basic, "",
                new String[] { "wav" }));
    }

    @Override
    public ActionType getActionType() {
        return ActionType.PLAY_SOUND;
    }

    @Override
    public void run() {
        var job = new Job("Play WAV file") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                var path = getAbsolutePath();
                monitor.beginTask("Connecting to " + path, IProgressMonitor.UNKNOWN);
                try {
                    URL url;
                    if (ResourceUtil.isExistingWorkspaceFile(path)) {
                        url = new File(ResourceUtil.workspacePathToSysPath(path).toOSString()).toURI().toURL();
                    } else if (ResourceUtil.isExistingLocalFile(path)) {
                        url = new File(path.toOSString()).toURI().toURL();
                    } else {
                        url = new URL(path.toString());
                    }

                    var clip = AudioSystem.getClip();
                    var in = AudioSystem.getAudioInputStream(url.openStream());
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });
                    clip.open(in);
                    clip.start();
                } catch (Exception e) {
                    OPIBuilderPlugin.getLogger().log(Level.SEVERE, "Failed to play WAV file " + getPath(), e);
                } finally {
                    monitor.done();
                }

                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private IPath getPath() {
        var path = (String) getPropertyValue(PROP_PATH);
        return path != null ? Path.fromPortableString(path) : null;
    }

    private IPath getAbsolutePath() {
        // read file
        var absolutePath = getPath();
        if (!getPath().isAbsolute()) {
            absolutePath = ResourceUtil.buildAbsolutePath(getWidgetModel(), getPath());
        }
        return absolutePath;
    }

    @Override
    public String getDefaultDescription() {
        return super.getDefaultDescription() + " " + getPath();
    }
}
