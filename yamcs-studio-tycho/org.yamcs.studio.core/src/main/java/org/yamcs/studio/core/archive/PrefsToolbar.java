package org.yamcs.studio.core.archive;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.yamcs.utils.TimeEncoding;

/*
 * Also used by CcsdsCompletessGui
 */
public class PrefsToolbar extends JPanel {
    private static final long serialVersionUID = 1L;
    DatePicker datePicker;
    Preferences prefs;
    public JButton reloadButton;

    public PrefsToolbar() {
        super(new FlowLayout(FlowLayout.LEFT));
        prefs = Preferences.userNodeForPackage(ArchivePanel.class);

        datePicker = new DatePicker();

        add(new JSeparator(SwingConstants.VERTICAL));

        reloadButton = new JButton("Reload");
        reloadButton.setActionCommand("reload");
        reloadButton.setEnabled(false);
        add(reloadButton);

        loadFromPrefs();
    }

    public void savePreferences() {
        prefs.putLong("rangeStart", getStartTimestamp());
        prefs.putLong("rangeEnd", getEndTimestamp());
    }

    public TimeInterval getInterval() {
        return datePicker.getInterval();
    }

    public long getEndTimestamp() {
        return datePicker.getEndTimestamp();
    }

    public long getStartTimestamp() {
        return datePicker.getStartTimestamp();
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
        datePicker.setStartTimestamp(prefs.getLong("rangeStart", TimeEncoding.currentInstant() - 30 * 24 * 3600));
        datePicker.setEndTimestamp(prefs.getLong("rangeEnd", TimeEncoding.currentInstant()));
    }

    public String[] getVisiblePackets() {
        String[] s = prefs.get("packets", "").split(" ");
        return s[0].equals("") ? new String[0] : s;
    }
}
