package org.yamcs.studio.core.archive;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;
import org.yamcs.YamcsException;
import org.yamcs.api.ConnectionListener;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.YamcsConnector;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;

public class ArchiveView extends ViewPart implements ArchiveIndexListener, ConnectionListener {

    ArchiveIndexReceiver indexReceiver;
    public ArchivePanel archivePanel;
    YamcsConnector yconnector;
    private String instance;

    @Override
    public void createPartControl(Composite parent) {
        yconnector = new YamcsConnector();
        indexReceiver = new YamcsArchiveIndexReceiver(yconnector);

        Composite locationComp = new Composite(parent, SWT.EMBEDDED);
        java.awt.Frame frame = SWT_AWT.new_Frame(locationComp);

        archivePanel = new ArchivePanel(this, false);

        archivePanel.setPreferredSize(new Dimension(300, 400));

        // While resizing, only update active item (slight performance gain)
        // When done resizing, update all
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                archivePanel.onWindowResizing();
            }
        });

        /*
         * addComponentListener(new ComponentAdapter() {
         * 
         * @Override public void componentResized(ComponentEvent e) {
         * archivePanel.onWindowResized(); } });
         */

        frame.add(archivePanel);

        indexReceiver.setIndexListener(this);
        yconnector.addConnectionListener(this);
        try {
            yconnector.connect(YamcsConnectData.parse("yamcs://machine:5445/simulator"));
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    public boolean isRefreshEnabled() {
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        RefreshCommandState commandState = (RefreshCommandState) service.getSourceProvider(RefreshCommandState.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(RefreshCommandState.STATE_KEY_ENABLED);
    }

    public void setRefreshEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            RefreshCommandState commandState = (RefreshCommandState) service.getSourceProvider(RefreshCommandState.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
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
                refresh();
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

    public void refresh(RefreshCommandState state) {
        System.out.println("incoming state is " + state);
        refresh();
    }

    private void refresh() {
        //debugLog("requestData() mark 1 "+new Date());
        archivePanel.startReloading();
        TimeInterval interval = archivePanel.getRequestedDataInterval();
        indexReceiver.getIndex(instance, interval);
    }

    @Override
    public void receiveTags(final List<ArchiveTag> tagList) {
        SwingUtilities.invokeLater(() -> archivePanel.tagsAdded(tagList));
    }

    @Override
    public void tagAdded(final ArchiveTag ntag) {
        SwingUtilities.invokeLater(() -> archivePanel.tagAdded(ntag));
    }

    @Override
    public void tagRemoved(final ArchiveTag rtag) {
        SwingUtilities.invokeLater(() -> archivePanel.tagRemoved(rtag));
    }

    @Override
    public void tagChanged(final ArchiveTag oldTag, final ArchiveTag newTag) {
        SwingUtilities.invokeLater(() -> archivePanel.tagChanged(oldTag, newTag));
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
