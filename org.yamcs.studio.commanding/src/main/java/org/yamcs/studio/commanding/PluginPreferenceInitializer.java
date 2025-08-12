/*******************************************************************************
 * Copyright (c) 2025 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding;

import static org.yamcs.studio.commanding.UIPreferences.STACK_ENTRY_SPLIT;
import static org.yamcs.studio.commanding.UIPreferences.intArrayToString;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * Plugin extension point to initialize the plugin runtime preferences.
 */
public class PluginPreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        var store = CommandingPlugin.getDefault().getPreferenceStore();

        store.setDefault(STACK_ENTRY_SPLIT, intArrayToString(new int[] { 500, 500 }));
    }
}
