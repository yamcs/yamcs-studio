package org.yamcs.studio.core.archive;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
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
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.utils.TimeEncoding;

public class ArchiveView extends ViewPart implements ArchiveIndexListener, ConnectionListener {

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

        archivePanel = new ArchivePanel(this, false);
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
        try {
            yconnector.connect(YamcsConnectData.parse("yamcs://machine:5445/simulator"));
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    private void createActions() {
        IActionBars bars = getViewSite().getActionBars();
        IMenuManager mgr = bars.getMenuManager();
        // There's probably a better way of doing this radio stuff, but going forward now
        List<Action> actions = new ArrayList<>();

        Action lastMonthAction = new Action("Last month", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                actions.forEach(action -> action.setChecked(action == this));
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -1);
                doFilter(TimeInterval.starting(TimeEncoding.fromCalendar(cal)));
            }
        };
        actions.add(lastMonthAction);
        mgr.add(lastMonthAction);

        Action last3MonthsAction = new Action("Last 3 months", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                actions.forEach(action -> action.setChecked(action == this));
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -3);
                doFilter(TimeInterval.starting(TimeEncoding.fromCalendar(cal)));
            }
        };
        actions.add(last3MonthsAction);
        mgr.add(last3MonthsAction);

        Action last12MonthsAction = new Action("Last 12 months", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                actions.forEach(action -> action.setChecked(action == this));
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -12);
                doFilter(TimeInterval.starting(TimeEncoding.fromCalendar(cal)));
            }
        };
        actions.add(last12MonthsAction);
        mgr.add(last12MonthsAction);

        Action customAction = new Action("Custom...", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                ;//handleFilter();
                actions.forEach(action -> action.setChecked(action == this));
            }
        };
        actions.add(customAction);
        mgr.add(customAction);
    }

    private void doFilter(TimeInterval range) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("will save range: " + TimeEncoding.toCombinedFormat(range.calculateStart()));
            System.out.println(".... ending " + TimeEncoding.toCombinedFormat(range.calculateStop()));
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
        System.out.println(".... refreshing with interval " + TimeEncoding.toCombinedFormat(interval.calculateStart()));
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
