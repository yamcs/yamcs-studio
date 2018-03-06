package org.yamcs.studio.eventlog;

import org.eclipse.jface.preference.IPreferenceStore;

public class EventLogPreferences {

    public static boolean isShowSequenceNumberColumn() {
        return getStore().getBoolean(PreferencePage.PREF_SHOW_SEQNUM_COL);
    }

    public static boolean isShowGenerationColumn() {
        return getStore().getBoolean(PreferencePage.PREF_SHOW_GENTIME_COL);
    }

    public static boolean isShowReceptionColumn() {
        return getStore().getBoolean(PreferencePage.PREF_SHOW_RECTIME_COL);
    }

    public static int getMessageLineCount() {
        return getStore().getInt(PreferencePage.PREF_LINECOUNT);
    }

    private static IPreferenceStore getStore() {
        return Activator.getDefault().getPreferenceStore();
    }
}
