/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import java.applet.Applet;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.properties.FilePathProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
        Job job = new Job("Play wave file") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                var path = getAbsolutePath();
                monitor.beginTask("Connecting to " + path, IProgressMonitor.UNKNOWN);
                try {
                    // a better way to play wav.
                    URL url;
                    if (ResourceUtil.isExistingWorkspaceFile(path)) {
                        url = new File(ResourceUtil.workspacePathToSysPath(path).toOSString()).toURI().toURL();
                    } else if (ResourceUtil.isExistingLocalFile(path)) {
                        url = new File(path.toOSString()).toURI().toURL();
                    } else {
                        url = new URL(path.toString());
                    }
                    Applet.newAudioClip(url).play();
                    // final InputStream in = ResourceUtil.pathToInputStream(
                    // getAbsolutePath(), false);
                    //
                    // UIJob playWavJob = new UIJob(getDescription()) {
                    // @Override
                    // public IStatus runInUIThread(IProgressMonitor monitor) {
                    // try {
                    // final BufferedInputStream bis;
                    // if (!(in instanceof BufferedInputStream))
                    // bis = new BufferedInputStream(in);
                    // else
                    // bis = (BufferedInputStream) in;
                    //
                    // final AudioInputStream stream = AudioSystem
                    // .getAudioInputStream(bis);
                    // Clip clip = AudioSystem.getClip();
                    // clip.open(stream);
                    //
                    // clip.addLineListener(new LineListener() {
                    // public void update(LineEvent event) {
                    // if (event.getType() == LineEvent.Type.STOP) {
                    // try {
                    // stream.close();
                    // bis.close();
                    // if (in != bis)
                    // in.close();
                    // } catch (IOException e) {
                    // OPIBuilderPlugin
                    // .getLogger()
                    // .log(Level.WARNING,
                    // "audio close error", e);
                    // }
                    // }
                    // }
                    // });
                    // clip.start();
                    // } catch (Exception e) {
                    // final String message = "Failed to play wave file " + getPath();
                    // OPIBuilderPlugin.getLogger().log(Level.WARNING,
                    // message, e);
                    // ConsoleService.getInstance().writeError(
                    // message + "\n" + e.getMessage());
                    // }
                    // return Status.OK_STATUS;
                    // }
                    // };
                    // playWavJob.schedule();
                } catch (Exception e) {
                    var message = "Failed to connect to wave file " + getPath();
                    OPIBuilderPlugin.getLogger().log(Level.SEVERE, message, e);
                } finally {
                    monitor.done();
                }

                return Status.OK_STATUS;
            }
        };
        job.schedule();

    }

    private IPath getPath() {
        return (IPath) getPropertyValue(PROP_PATH);
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
