package org.yamcs.studio.eventlog;

import org.eclipse.jface.preference.IPreferenceStore;

public class EventLogPreferences {

    public static int getMessageLineCount() {
        return getStore().getInt(PreferencePage.PREF_LINECOUNT);
    }

    private static IPreferenceStore getStore() {
        return Activator.getDefault().getPreferenceStore();
    }
}
