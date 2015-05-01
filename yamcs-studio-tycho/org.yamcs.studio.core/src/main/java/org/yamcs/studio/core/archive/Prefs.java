package org.yamcs.studio.core.archive;

import java.util.prefs.Preferences;

import org.yamcs.utils.TimeEncoding;

public class Prefs {
    private static final long serialVersionUID = 1L;
    Preferences prefs;

    private long startTimestamp = 0;
    private long endTimestamp = TimeEncoding.currentInstant();

    public Prefs() {
        prefs = Preferences.userNodeForPackage(ArchivePanel.class);
        loadFromPrefs();
    }

    public void savePreferences() {
        prefs.putLong("rangeStart", getStartTimestamp());
        prefs.putLong("rangeEnd", getEndTimestamp());
    }

    public TimeInterval getInterval() {
        TimeInterval interval = new TimeInterval();
        interval.setStart(startTimestamp);
        interval.setStop(endTimestamp);
        return interval;
    }

    public long getEndTimestamp() {
        return TimeEncoding.currentInstant();
    }

    public long getStartTimestamp() {
        return 0;
    }

    public void setVisiblePackets(Object[] packets) {
        StringBuffer strbuf = new StringBuffer();
        for (Object s : packets) {
            strbuf.append(" ");
            strbuf.append(s.toString());
        }
        prefs.put("packets", strbuf.toString().trim());
    }

    void loadFromPrefs() {
        startTimestamp = prefs.getLong("rangeStart", TimeEncoding.currentInstant() - 30 * 24 * 3600);
        endTimestamp = prefs.getLong("rangeEnd", TimeEncoding.currentInstant());
    }

    public String[] getVisiblePackets() {
        String[] s = prefs.get("packets", "").split(" ");
        return s[0].equals("") ? new String[0] : s;
    }
}
