/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.archive;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * Used in plugin.xml core-expressions to keep track of enabled/disabled state of the refresh-archive button in the
 * ArchiveView toolbar.
 */
public class RefreshStateProvider extends AbstractSourceProvider {

    public static final String STATE_KEY_ENABLED = "org.yamcs.studio.archive.state.refreshEnabled";
    private static final String[] SOURCE_NAMES = { STATE_KEY_ENABLED };

    private boolean enabled = false;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        fireSourceChanged(ISources.WORKBENCH, getCurrentState());
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map getCurrentState() {
        Map map = new HashMap(1);
        map.put(STATE_KEY_ENABLED, enabled);
        return map;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return SOURCE_NAMES;
    }

    @Override
    public void dispose() {
    }
}
