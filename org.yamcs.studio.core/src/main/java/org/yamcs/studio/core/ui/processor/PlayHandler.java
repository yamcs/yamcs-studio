/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.processor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Currently only resumes a paused replay. Should eventually also seek to the beginning and replay a stopped replay. We
 * should probably do this at the server level, rather than stitching it in here.
 */
public class PlayHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var processor = YamcsPlugin.getProcessorClient();
        processor.resume();
        return null;
    }
}
