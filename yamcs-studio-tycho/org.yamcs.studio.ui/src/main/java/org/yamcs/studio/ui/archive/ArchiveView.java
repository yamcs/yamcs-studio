package org.yamcs.studio.ui.archive;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISourceProviderListener;
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
import org.yamcs.protobuf.Yamcs.TimeInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.ManagementCatalogue;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.TimeCatalogue;
import org.yamcs.studio.core.TimeListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.YamcsUIPlugin;
import org.yamcs.studio.ui.connections.ConnectionStateProvider;
import org.yamcs.studio.ui.processor.ProcessorStateProvider;
import org.yamcs.utils.TimeEncoding;

public class ArchiveView extends ViewPart
        implements StudioConnectionListener, TimeListener, ProcessorListener, ISourceProviderListener, ConnectionListener {

    ArchiveIndexReceiver indexReceiver;
    public ArchivePanel archivePanel;
    YamcsConnector yconnector;
    private String instance;
    private ResourceManager resourceManager;

    private Label replayTimeLabel;
    private Composite replayComposite;
    private GridData replayCompositeGridData;

    private Image reverseImage;
    private Image jumpLeftImage;
    private Image playImage;
    private Image pauseImage;
    private Image jumpRightImage;
    private Image forwardImage;
    private Image forward2xImage;
    private Image forward4xImage;
    private Image forward8xImage;
    private Image forward16xImage;

    private Button reverseButton;
    private Button jumpLeftButton;
    private Button playButton;
    private Button jumpRightButton;
    private Button forwardButton;

    private ProcessorInfo processorInfo;

    @SuppressWarnings("rawtypes")
    private Map combinedState = new HashMap();

    @Override
    public void createPartControl(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        reverseImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/reverse.png"));
        jumpLeftImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/first.png"));
        playImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/play.png"));
        pauseImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/pause.png"));
        jumpRightImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/last.png"));
        forwardImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward.png"));
        forward2xImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward2x.png"));
        forward4xImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward4x.png"));
        forward8xImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward8x.png"));
        forward16xImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward16x.png"));

        createActions();

        yconnector = new YamcsConnector(false);
        indexReceiver = new ArchiveIndexReceiver(yconnector);

        parent.setLayout(new FillLayout());

        Composite contentArea = new Composite(parent, SWT.NONE);

        contentArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        contentArea.setLayout(gl);

        Composite locationComp = new Composite(contentArea, SWT.EMBEDDED);
        locationComp.setLayoutData(new GridData(GridData.FILL_BOTH));
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

        replayComposite = new Composite(contentArea, SWT.NONE);
        replayCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
        replayComposite.setLayoutData(replayCompositeGridData);
        gl = new GridLayout(3, true);
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        replayComposite.setLayout(gl);

        replayTimeLabel = new Label(replayComposite, SWT.NONE);
        FontData[] fd = replayTimeLabel.getFont().getFontData();
        fd[0].setHeight(fd[0].getHeight() - 2);
        replayTimeLabel.setFont(new Font(parent.getDisplay(), fd)); // TODO dispose this font!
        replayTimeLabel.setText("                             "); // ugh...
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.LEFT;
        gd.grabExcessHorizontalSpace = true;
        replayTimeLabel.setLayoutData(gd);

        Composite controlsComposite = new Composite(replayComposite, SWT.NONE);
        gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        controlsComposite.setLayoutData(gd);
        gl = new GridLayout(5, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        controlsComposite.setLayout(gl);

        reverseButton = new Button(controlsComposite, SWT.PUSH);
        reverseButton.setImage(reverseImage);
        reverseButton.setEnabled(false);
        reverseButton.setToolTipText("Reverse");

        jumpLeftButton = new Button(controlsComposite, SWT.PUSH);
        jumpLeftButton.setImage(jumpLeftImage);
        jumpLeftButton.setToolTipText("Jump Left");

        playButton = new Button(controlsComposite, SWT.PUSH);
        playButton.setImage(playImage);
        playButton.setToolTipText("Play");

        jumpRightButton = new Button(controlsComposite, SWT.PUSH);
        jumpRightButton.setImage(jumpRightImage);
        jumpRightButton.setToolTipText("Jump Right");

        forwardButton = new Button(controlsComposite, SWT.PUSH);
        forwardButton.setImage(forwardImage);
        forwardButton.setToolTipText("Forward");

        Link link = new Link(replayComposite, SWT.NONE);
        //link.setText("<a>Return to realtime</a>");
        link.addListener(SWT.Selection, evt -> {
            System.out.println("Selection: " + evt.text);
        });
        gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        link.setLayoutData(gd);

        toggleReplayComposite(false);

        ISourceProviderService service = getSite().getService(ISourceProviderService.class);
        ProcessorStateProvider processorState = (ProcessorStateProvider) service.getSourceProvider(ProcessorStateProvider.STATE_KEY_PROCESSING);
        processorState.addSourceProviderListener(this);
        ConnectionStateProvider connectionState = (ConnectionStateProvider) service.getSourceProvider(ConnectionStateProvider.STATE_KEY_CONNECTED);
        connectionState.addSourceProviderListener(this);

        indexReceiver.setIndexListener(this);
        yconnector.addConnectionListener(this);
        //ManagementCatalogue.getInstance().addProcessorListener(this);
        ConnectionManager.getInstance().addStudioConnectionListener(this);
    }

    private void toggleReplayComposite(boolean enabled) {
        if (replayComposite.isDisposed())
            return;
        replayCompositeGridData.exclude = !enabled;
        replayComposite.setVisible(enabled);
        replayComposite.getParent().layout();
    }

    @Override
    public void onStudioConnect(YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restClient, WebSocketRegistrar webSocketClient) {
        yconnector.connect(hornetqProps);
        if (webSocketClient != null) {
            webSocketClient.addTimeListener(this);
        }
    }

    @Override
    public void onStudioDisconnect() {
        yconnector.disconnect();
        SwingUtilities.invokeLater(() -> {
            archivePanel.getDataViewer().getDataView()
                    .setCurrentLocator(archivePanel.getDataViewer().getDataView().DO_NOT_DRAW);
        });
    }

    @Override
    public void processTime(TimeInfo timeInfo) {
        SwingUtilities.invokeLater(() -> {
            archivePanel.getDataViewer().getDataView().setCurrentLocator(timeInfo.getCurrentTime());
        });
        Display.getDefault().asyncExec(() -> {
            if (replayTimeLabel.isDisposed())
                return;
            long missionTime = timeInfo.getCurrentTime();
            if (missionTime == TimeEncoding.INVALID_INSTANT || missionTime == 0) {
                replayTimeLabel.setText("");
            } else {
                String prettyTime = TimeCatalogue.getInstance().toString(timeInfo.getCurrentTime());
                replayTimeLabel.setText(prettyTime);
            }
        });
    }

    private void createActions() {
        IActionBars bars = getViewSite().getActionBars();
        IMenuManager mgr = bars.getMenuManager();
        mgr.add(new Action("Last day", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    Calendar cal = TimeCatalogue.getInstance().getMissionTimeAsCalendar(true);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    doFilter(TimeInterval.starting(TimeEncoding.fromCalendar(cal)));
                }
            }
        });
        mgr.add(new Action("Last week", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    Calendar cal = TimeCatalogue.getInstance().getMissionTimeAsCalendar(true);
                    cal.add(Calendar.DAY_OF_MONTH, -7);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    doFilter(TimeInterval.starting(TimeEncoding.fromCalendar(cal)));
                }
            }
        });
        mgr.add(new Action("Last month", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    Calendar cal = TimeCatalogue.getInstance().getMissionTimeAsCalendar(true);
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
                    Calendar cal = TimeCatalogue.getInstance().getMissionTimeAsCalendar(true);
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
                    Calendar cal = TimeCatalogue.getInstance().getMissionTimeAsCalendar(true);
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
        RefreshStateProvider commandState = (RefreshStateProvider) service.getSourceProvider(RefreshStateProvider.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(RefreshStateProvider.STATE_KEY_ENABLED);
    }

    public void setRefreshEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            RefreshStateProvider commandState = (RefreshStateProvider) service.getSourceProvider(RefreshStateProvider.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public boolean isZoomInEnabled() {
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        ZoomInStateProvider commandState = (ZoomInStateProvider) service.getSourceProvider(ZoomInStateProvider.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(ZoomInStateProvider.STATE_KEY_ENABLED);
    }

    public void setZoomInEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            ZoomInStateProvider commandState = (ZoomInStateProvider) service.getSourceProvider(ZoomInStateProvider.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public boolean isZoomOutEnabled() {
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        ZoomOutStateProvider commandState = (ZoomOutStateProvider) service.getSourceProvider(ZoomOutStateProvider.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(ZoomOutStateProvider.STATE_KEY_ENABLED);
    }

    public void setZoomOutEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            ZoomOutStateProvider commandState = (ZoomOutStateProvider) service.getSourceProvider(ZoomOutStateProvider.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public boolean isZoomClearEnabled() {
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        ClearZoomStateProvider commandState = (ClearZoomStateProvider) service.getSourceProvider(ClearZoomStateProvider.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(ClearZoomStateProvider.STATE_KEY_ENABLED);
    }

    public void setZoomClearEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            ClearZoomStateProvider commandState = (ClearZoomStateProvider) service.getSourceProvider(ClearZoomStateProvider.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public boolean isTagEnabled() {
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        AnnotateRangeStateProvider commandState = (AnnotateRangeStateProvider) service
                .getSourceProvider(AnnotateRangeStateProvider.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(AnnotateRangeStateProvider.STATE_KEY_ENABLED);
    }

    public void setTagEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            AnnotateRangeStateProvider commandState = (AnnotateRangeStateProvider) service
                    .getSourceProvider(AnnotateRangeStateProvider.STATE_KEY_ENABLED);
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

    public void receiveArchiveRecords(IndexResult ir) {
        archivePanel.receiveArchiveRecords(ir);
    }

    public void receiveArchiveRecordsError(String errorMessage) {
        archivePanel.receiveArchiveRecordsError(errorMessage);
    }

    public void receiveArchiveRecordsFinished() {
        if (indexReceiver.supportsTags()) {
            TimeInterval interval = archivePanel.getRequestedDataInterval();
            indexReceiver.getTag(interval);
        } else {
            archivePanel.archiveLoadFinished();
        }
    }

    public void receiveTagsFinished() {
        archivePanel.archiveLoadFinished();
    }

    public void refresh() {
        archivePanel.startReloading();
        TimeInterval interval = archivePanel.getRequestedDataInterval();
        indexReceiver.getIndex(instance, interval);
    }

    public void receiveTags(final List<ArchiveTag> tagList) {
        SwingUtilities.invokeLater(() -> archivePanel.tagsAdded(tagList));
    }

    public void tagAdded(final ArchiveTag ntag) {
        SwingUtilities.invokeLater(() -> archivePanel.tagAdded(ntag));
    }

    public void tagRemoved(final ArchiveTag rtag) {
        SwingUtilities.invokeLater(() -> archivePanel.tagRemoved(rtag));
    }

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

    @Override
    public void clientUpdated(ClientInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (updatedInfo.getCurrentClient()) {
                ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
                processorInfo = catalogue.getProcessorInfo(updatedInfo.getProcessorName());
                boolean showReplayControls = !processorInfo.getName().equals("realtime");
                toggleReplayComposite(showReplayControls);
            }
        });
    }

    @Override
    public void clientDisconnected(ClientInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (updatedInfo.getCurrentClient()) {
                processorInfo = null;
                toggleReplayComposite(false);
            }
        });
    }

    @Override
    public void processorUpdated(ProcessorInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (processorInfo != null && updatedInfo.getName().equals(processorInfo.getName())) {
                processorInfo = updatedInfo;
            }
        });
    }

    @Override
    public void statisticsUpdated(Statistics stats) {
    }

    @Override
    public void processorClosed(ProcessorInfo processorInfo) {
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
        for (Object entry : sourceValuesByName.entrySet()) {
            combinedState.put(((Entry) entry).getKey(), ((Entry) entry).getValue());
        }
        updateState();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sourceChanged(int sourcePriority, String sourceName, Object sourceValue) {
        combinedState.put(sourceName, sourceValue);
    }

    /*
     * We use the state available in the workbench-level state provider, which may have a need in
     * the future. This way we avoid duplicating similar logic here.
     */
    private void updateState() {
        Boolean connected = (Boolean) combinedState.get(ConnectionStateProvider.STATE_KEY_CONNECTED);
        String processing = (String) combinedState.get(ProcessorStateProvider.STATE_KEY_PROCESSING);
        Boolean replay = (Boolean) combinedState.get(ProcessorStateProvider.STATE_KEY_REPLAY);
        if (connected == null || processing == null || replay == null)
            return;

        toggleReplayComposite(replay);

        boolean reverseEnabled = false;
        //boolean reverseEnabled = (Boolean.TRUE.equals(connected));
        //reverseEnabled &= ("RUNNING".equals(processing));
        //reverseEnabled &= (Boolean.TRUE.equals(replay));

        boolean jumpLeftEnabled = (Boolean.TRUE.equals(connected));
        jumpLeftEnabled &= ("RUNNING".equals(processing));
        jumpLeftEnabled &= (Boolean.TRUE.equals(replay));

        boolean playEnabled = (Boolean.TRUE.equals(connected));
        playEnabled &= ("PAUSED".equals(processing) | "STOPPED".equals(processing));
        playEnabled &= (Boolean.TRUE.equals(replay));

        boolean pauseEnabled = (Boolean.TRUE.equals(connected));
        pauseEnabled &= ("RUNNING".equals(processing));
        pauseEnabled &= (Boolean.TRUE.equals(replay));

        boolean jumpRightEnabled = (Boolean.TRUE.equals(connected));
        jumpRightEnabled &= ("RUNNING".equals(processing));
        jumpRightEnabled &= (Boolean.TRUE.equals(replay));

        boolean forwardEnabled = (Boolean.TRUE.equals(connected));
        forwardEnabled &= ("RUNNING".equals(processing));
        forwardEnabled &= (Boolean.TRUE.equals(replay));

        reverseButton.setEnabled(reverseEnabled);
        jumpLeftButton.setEnabled(jumpLeftEnabled);
        playButton.setEnabled(playEnabled || pauseEnabled);
        jumpRightButton.setEnabled(jumpRightEnabled);
        forwardButton.setEnabled(forwardEnabled);

        boolean playVisible = ("STOPPED".equals(processing));
        playVisible |= ("ERROR".equals(processing));
        playVisible |= ("PAUSED".equals(processing));
        playVisible |= ("CLOSED".equals(processing));

        if (playVisible) {
            playButton.setImage(playImage);
            playButton.setToolTipText("Resume Processing");
        } else {
            playButton.setImage(pauseImage);
            playButton.setToolTipText("Pause Processing");
        }
    }
}
