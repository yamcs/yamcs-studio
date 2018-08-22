package org.yamcs.studio.eventlog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
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
        IDialogSettings settings = getDialogSettings();
        IDialogSettings section = settings.getSection("eventlog-table");
        if (section == null) {
            section = settings.addNewSection("eventlog-table");
        }
        return section;
    }

    public int getMessageLineCount() {
        return getPreferenceStore().getInt(PreferencePage.PREF_LINECOUNT);
    }

    public void storeColoringRules(List<ColoringRule> rules) {
        IPreferenceStore store = getPreferenceStore();

        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (ColoringRule rule : rules) {
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
        IPreferenceStore store = getPreferenceStore();
        String joined = store.getString(PreferencePage.PREF_RULES);
        return composeColoringRules(joined);
    }

    public List<ColoringRule> loadDefaultColoringRules() {
        IPreferenceStore store = getPreferenceStore();
        String joined = store.getDefaultString(PreferencePage.PREF_RULES);
        return composeColoringRules(joined);
    }

    public List<ColoringRule> composeColoringRules(String joined) {
        if (joined.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
            return new ArrayList<>(0);
        }

        List<ColoringRule> rules = new ArrayList<>();
        for (String ruleString : joined.split(";")) {
            String[] parts = ruleString.split("@");
            RGB bg = StringConverter.asRGB(parts[1]);
            RGB fg = StringConverter.asRGB(parts[2]);
            rules.add(new ColoringRule(parts[0], bg, fg));
        }
        return rules;
    }
}
