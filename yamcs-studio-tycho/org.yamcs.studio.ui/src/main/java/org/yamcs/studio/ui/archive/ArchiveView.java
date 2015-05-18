package org.yamcs.studio.ui.archive;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Calendar;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;
import org.yamcs.YamcsException;
import org.yamcs.api.ConnectionListener;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.YamcsConnector;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.ui.YamcsUIPlugin;
import org.yamcs.utils.TimeEncoding;

public class ArchiveView extends ViewPart implements StudioConnectionListener, ArchiveIndexListener, ConnectionListener {

    ArchiveIndexReceiver indexReceiver;
    public ArchivePanel archivePanel;
    YamcsConnector yconnector;
    private String instance;

    @Override
    public void createPartControl(Composite parent) {
        createActions();

        yconnector = new YamcsConnector();
        indexReceiver = new YamcsArchiveIndexReceiver(yconnector);

        Composite locationComp = new Composite(parent, SWT.EMBEDDED);
        java.awt.Frame frame = SWT_AWT.new_Frame(locationComp);

        archivePanel = new ArchivePanel(this);
        archivePanel.setPreferredSize(new Dimension(300, 400));

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                archivePanel.onWindowResized();
            }
        });

        frame.add(archivePanel);

        indexReceiver.setIndexListener(this);
        yconnector.addConnectionListener(this);
        YamcsPlugin.getDefault().addStudioConnectionListener(this);

        ArchiveProcessorListener processorListener = new ArchiveProcessorListener(parent.getDisplay(), archivePanel.getDataViewer());
        YamcsPlugin.getDefault().addProcessorListener(processorListener);
    }

    /**
     * Called when we get green light from YamcsPlugin
     */
    @Override
    public void processConnectionInfo(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps) {
        yconnector.connect(hornetqProps);
    }

    /**
     * Called when YamcsPlugin wants this connection to stop (might be resumed latter with
     * processConnectionInfo)
     */
    @Override
    public void disconnect() {
        yconnector.disconnect();
    }

    private void createActions() {
        IActionBars bars = getViewSite().getActionBars();
        IMenuManager mgr = bars.getMenuManager();
        mgr.add(new Action("Last month", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    Calendar cal = Calendar.getInstance(YamcsUIPlugin.getDefault().getTimeZone());
                    cal.add(Calendar.MONTH, -1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    doFilter(TimeInterval.starting(TimeEncoding.fromCalendar(cal)));
                }
            }
        });
        mgr.add(new Action("Last 3 months", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    Calendar cal = Calendar.getInstance(YamcsUIPlugin.getDefault().getTimeZone());
                    cal.add(Calendar.MONTH, -3);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    doFilter(TimeInterval.starting(TimeEncoding.fromCalendar(cal)));
                }
            }
        });
        mgr.add(new Action("Last 12 months", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    Calendar cal = Calendar.getInstance(YamcsUIPlugin.getDefault().getTimeZone());
                    cal.add(Calendar.MONTH, -12);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    doFilter(TimeInterval.starting(TimeEncoding.fromCalendar(cal)));
                }
            }
        });
        mgr.add(new Action("Custom...", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    CustomizeRangeDialog dialog = new CustomizeRangeDialog(Display.getCurrent().getActiveShell());
                    dialog.setInitialRange(archivePanel.prefs.getInterval());
                    if (dialog.open() == Window.OK) {
                        TimeInterval range = new TimeInterval();
                        if (dialog.hasStartTime())
                            range.setStart(dialog.getStartTime());
                        if (dialog.hasStopTime())
                            range.setStop(dialog.getStopTime());
                        doFilter(range);
                    }
                }
            }
        });
    }

    private void doFilter(TimeInterval range) {
        SwingUtilities.invokeLater(() -> {
            archivePanel.prefs.saveRange(range);
            refresh();
        });
    }

    public boolean isRefreshEnabled() {
        // Not necessarily on the SWT thread. This is a bit of a risk. Maybe we should do a blocking Display.getDefault().syncExec
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

    public boolean isZoomInEnabled() {
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        ZoomInCommandState commandState = (ZoomInCommandState) service.getSourceProvider(ZoomInCommandState.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(ZoomInCommandState.STATE_KEY_ENABLED);
    }

    public void setZoomInEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            ZoomInCommandState commandState = (ZoomInCommandState) service.getSourceProvider(ZoomInCommandState.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public boolean isZoomOutEnabled() {
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        ZoomOutCommandState commandState = (ZoomOutCommandState) service.getSourceProvider(ZoomOutCommandState.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(ZoomOutCommandState.STATE_KEY_ENABLED);
    }

    public void setZoomOutEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            ZoomOutCommandState commandState = (ZoomOutCommandState) service.getSourceProvider(ZoomOutCommandState.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public boolean isZoomClearEnabled() {
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        ZoomClearCommandState commandState = (ZoomClearCommandState) service.getSourceProvider(ZoomClearCommandState.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(ZoomClearCommandState.STATE_KEY_ENABLED);
    }

    public void setZoomClearEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            ZoomClearCommandState commandState = (ZoomClearCommandState) service.getSourceProvider(ZoomClearCommandState.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public boolean isTagEnabled() {
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        TagCommandState commandState = (TagCommandState) service.getSourceProvider(TagCommandState.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(TagCommandState.STATE_KEY_ENABLED);
    }

    public void setTagEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            TagCommandState commandState = (TagCommandState) service.getSourceProvider(TagCommandState.STATE_KEY_ENABLED);
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

    public void refresh() {
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
