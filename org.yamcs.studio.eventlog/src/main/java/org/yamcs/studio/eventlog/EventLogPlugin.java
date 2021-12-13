/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.eventlog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class EventLogPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.eventlog";

    private static EventLogPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    public static EventLogPlugin getDefault() {
        return plugin;
    }

    public ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public IDialogSettings getCommandHistoryTableSettings() {
        var settings = getDialogSettings();
        var section = settings.getSection("eventlog-table");
        if (section == null) {
            section = settings.addNewSection("eventlog-table");
        }
        return section;
    }

    public int getMessageLineCount() {
        return getPreferenceStore().getInt(PreferencePage.PREF_LINECOUNT);
    }

    public void storeColoringRules(List<ColoringRule> rules) {
        var store = getPreferenceStore();

        var buf = new StringBuilder();
        var first = true;
        for (var rule : rules) {
            if (!first) {
                buf.append(";"); // Same ENTRY_SEPARATOR as used in jface PreferenceConverter
            }
            first = false;
            buf.append(rule.expression);
            buf.append("@").append(StringConverter.asString(rule.bg));
            buf.append("@").append(StringConverter.asString(rule.fg));
        }

        store.setValue(PreferencePage.PREF_RULES, buf.toString());
    }

    public List<ColoringRule> loadColoringRules() {
        var store = getPreferenceStore();
        var joined = store.getString(PreferencePage.PREF_RULES);
        return composeColoringRules(joined);
    }

    public List<ColoringRule> loadDefaultColoringRules() {
        var store = getPreferenceStore();
        var joined = store.getDefaultString(PreferencePage.PREF_RULES);
        return composeColoringRules(joined);
    }

    public List<ColoringRule> composeColoringRules(String joined) {
        if (joined.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
            return new ArrayList<>(0);
        }

        List<ColoringRule> rules = new ArrayList<>();
        for (var ruleString : joined.split(";")) {
            var parts = ruleString.split("@");
            var bg = StringConverter.asRGB(parts[1]);
            var fg = StringConverter.asRGB(parts[2]);
            rules.add(new ColoringRule(parts[0], bg, fg));
        }
        return rules;
    }
}
