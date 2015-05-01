package org.yamcs.studio.core.archive;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.YamcsException;
import org.yamcs.api.ConnectionListener;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.YamcsConnector;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;

public class ArchiveView extends ViewPart implements ArchiveIndexListener, ConnectionListener, ActionListener {

    ArchiveIndexReceiver indexReceiver;
    public ArchivePanel archivePanel;
    YamcsConnector yconnector;
    private String instance;

    @Override
    public void createPartControl(Composite parent) {
        yconnector = new YamcsConnector();
        indexReceiver = new YamcsArchiveIndexReceiver(yconnector);

        indexReceiver.setIndexListener(this);
        yconnector.addConnectionListener(this);
        try {
            yconnector.connect(YamcsConnectData.parse("yamcs://machine:5445/simulator"));
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }

        Composite locationComp = new Composite(parent, SWT.EMBEDDED);
        java.awt.Frame frame = SWT_AWT.new_Frame(locationComp);

        /*
         * BUTTONS
         */
        archivePanel = new ArchivePanel(this, false);
        archivePanel.prefs.reloadButton.addActionListener(this);

        archivePanel.setPreferredSize(new Dimension(300, 400));

        // While resizing, only update active item (slight performance gain)
        // When done resizing, update all
        /*
         * frame.addComponentListener(new ComponentAdapter() {
         *
         * @Override public void componentResized(ComponentEvent e) {
         * archivePanel.onWindowResizing(); } });
         */
        /*
         * addComponentListener(new ComponentAdapter() {
         * 
         * @Override public void componentResized(ComponentEvent e) {
         * archivePanel.onWindowResized(); } });
         */

        frame.add(archivePanel);
    }

    protected void showMessage(String msg) {
        System.out.println(msg);
        //JOptionPane.showMessageDialog(this, msg, getTitle(), JOptionPane.PLAIN_MESSAGE);
    }

    protected void showInfo(String msg) {
        System.out.println(msg);
        //JOptionPane.showMessageDialog(this, msg, getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    protected void showError(String msg) {
        System.out.println(msg);
        //JOptionPane.showMessageDialog(this, msg, getTitle(), JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void connecting(String url) {
        log("Connecting to " + url);
    }

    @Override
    public void connected(String url) {
        try {
            List<String> instances = yconnector.getYamcsInstances();
            if (instances != null) {
                archivePanel.connected();
                requestData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionFailed(String url, YamcsException exception) {
        archivePanel.disconnected();
    }

    @Override
    public void disconnected() {
        archivePanel.disconnected();
    }

    @Override
    public void log(String text) {
        System.out.println(text);
    }

    public void popup(String text) {
        showMessage(text);
    }

    @Override
    public void receiveArchiveRecords(IndexResult ir) {
        archivePanel.receiveArchiveRecords(ir);
    }

    @Override
    public void receiveArchiveRecordsError(String errorMessage) {
        archivePanel.receiveArchiveRecordsError(errorMessage);
    }

    @Override
    public void receiveArchiveRecordsFinished() {
        if (indexReceiver.supportsTags()) {
            TimeInterval interval = archivePanel.getRequestedDataInterval();
            indexReceiver.getTag(instance, interval);
        } else {
            archivePanel.archiveLoadFinished();
        }
    }

    @Override
    public void receiveTagsFinished() {
        archivePanel.archiveLoadFinished();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        final String cmd = ae.getActionCommand();
        if (cmd.equalsIgnoreCase("reload")) {
            requestData();
        } else if (cmd.equals("hide_resp")) {
            //  buildPopup();
        } else if (cmd.equalsIgnoreCase("exit")) {
            System.exit(0);
        }
    }

    private void requestData() {
        //debugLog("requestData() mark 1 "+new Date());
        archivePanel.startReloading();
        TimeInterval interval = archivePanel.getRequestedDataInterval();
        indexReceiver.getIndex(instance, interval);
    }

    @Override
    public void receiveTags(final List<ArchiveTag> tagList) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                archivePanel.tagsAdded(tagList);
            }
        });
    }

    @Override
    public void tagAdded(final ArchiveTag ntag) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                archivePanel.tagAdded(ntag);
            }
        });
    }

    @Override
    public void tagRemoved(final ArchiveTag rtag) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                archivePanel.tagRemoved(rtag);
            }
        });
    }

    @Override
    public void tagChanged(final ArchiveTag oldTag, final ArchiveTag newTag) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                archivePanel.tagChanged(oldTag, newTag);
            }
        });

    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        super.dispose();

        if (yconnector != null)
            yconnector.disconnect();
    }
}
