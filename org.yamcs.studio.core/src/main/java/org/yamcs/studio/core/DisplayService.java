/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core;

import java.util.Objects;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;

public class DisplayService implements YamcsAware, PluginService {

    private static final Logger log = Logger.getLogger(DisplayService.class.getName());

    private String previousInstance;
    private String previousProcessor;

    public DisplayService() {
        YamcsPlugin.addListener(this);
    }

    @Override
    public void changeProcessor(String instance, String processor) {
        // Reduce the number of events
        var realChange = !Objects.equals(instance, previousInstance) || !Objects.equals(processor, previousProcessor);

        previousInstance = instance;
        previousProcessor = processor;

        if (realChange) {
            // What we really want is that all the widgets lose their values, so
            // that they wouldn't get restored on another processor or connection.

            // But from the way CSS-BOY is built, the only way to achieve that
            // is to reset the displays. (widgets ignore "null" value updates)
            // and only show a 'disconnected' frame.

            var display = Display.getDefault();
            if (display != null && !display.isDisposed()) {
                display.asyncExec(() -> {
                    log.fine("No processor: resetting display state");
                    RCPUtils.runCommand("org.csstudio.opibuilder.refreshAllDisplays");
                });
            }
        }
    }

    @Override
    public void dispose() {
        YamcsPlugin.removeListener(this);
    }
}
