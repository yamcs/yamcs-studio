package org.yamcs.studio.core.ui.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.prefs.Preferences;

import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.model.TimeCatalogue;

/**
 * TODO use gson for better migrations, or use eclipse prefs
 */
public class Prefs {

    private Preferences prefs = Preferences.userNodeForPackage(Prefs.class);

    public void saveRange(TimeInterval range) {
        putObject(prefs, "archiveRange2", range);
    }

    public TimeInterval getInterval() {
        TimeInterval range;
        try {
            range = (TimeInterval) getObject(prefs, "archiveRange2");
        } catch (ClassNotFoundException e) {
            range = null; // Keep this around for a while until we migrate prefs
                          // to gson. TimeInterval was moved multiple times.
        }
        if (range == null) {
            Instant missionTime = TimeCatalogue.getInstance().getMissionTime(true);
            range = TimeInterval.starting(missionTime.minus(1, ChronoUnit.MONTHS));
        }
        return range;
    }

    public void setVisiblePackets(Object[] packets) {
        StringBuilder strbuf = new StringBuilder();
        for (Object s : packets) {
            strbuf.append(" ");
            strbuf.append(s.toString());
        }
        prefs.put("packets", strbuf.toString().trim());
    }

    public String[] getVisiblePackets() {
        String[] s = prefs.get("packets", "").split(" ");
        return s[0].equals("") ? new String[0] : s;
    }

    static public void putObject(Preferences prefs, String key, Object o) {
        try {
            byte[] raw = object2Bytes(o);
            prefs.putByteArray(key, raw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public Object getObject(Preferences prefs, String key) throws ClassNotFoundException {
        byte[] raw = prefs.getByteArray(key, null);
        if (raw == null) {
            return null;
        }
        try {
            return bytes2Object(raw);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static private byte[] object2Bytes(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    static private Object bytes2Object(byte[] raw) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);
        return new ObjectInputStream(bais).readObject();
    }
}
