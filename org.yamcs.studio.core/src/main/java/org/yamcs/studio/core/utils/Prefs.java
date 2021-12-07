/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.temporal.ChronoUnit;
import java.util.prefs.Preferences;

import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;

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
            var missionTime = YamcsPlugin.getMissionTime(true);
            range = TimeInterval.starting(missionTime.minus(30, ChronoUnit.DAYS));
        }
        return range;
    }

    public void setVisiblePackets(Object[] packets) {
        var strbuf = new StringBuilder();
        for (Object s : packets) {
            strbuf.append(" ");
            strbuf.append(s.toString());
        }
        prefs.put("packets", strbuf.toString().trim());
    }

    public String[] getVisiblePackets() {
        var s = prefs.get("packets", "").split(" ");
        return s[0].equals("") ? new String[0] : s;
    }

    static public void putObject(Preferences prefs, String key, Object o) {
        try {
            var raw = object2Bytes(o);
            prefs.putByteArray(key, raw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public Object getObject(Preferences prefs, String key) throws ClassNotFoundException {
        var raw = prefs.getByteArray(key, null);
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
        var baos = new ByteArrayOutputStream();
        var oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    static private Object bytes2Object(byte[] raw) throws IOException, ClassNotFoundException {
        var bais = new ByteArrayInputStream(raw);
        return new ObjectInputStream(bais).readObject();
    }
}
