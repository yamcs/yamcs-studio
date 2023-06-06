/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.yamcs.studio.core.YamcsPlugin;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConfigurationStateProvider extends AbstractSourceProvider {

    public static final String STATE_KEY_SPELL_ENABLED = "org.yamcs.studio.ui.state.spellEnabled";
    private static final String[] SOURCE_NAMES = { STATE_KEY_SPELL_ENABLED };

    @Override
    public Map getCurrentState() {
        var map = new HashMap(1);
        map.put(STATE_KEY_SPELL_ENABLED, YamcsPlugin.getDefault().isSpellEnabled());
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
