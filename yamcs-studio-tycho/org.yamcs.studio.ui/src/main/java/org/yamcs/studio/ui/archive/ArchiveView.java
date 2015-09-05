package org.yamcs.studio.ui.archive;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.model.TimeListener;
import org.yamcs.studio.core.web.WebSocketRegistrar;
import org.yamcs.studio.ui.RCPUtils;
import org.yamcs.studio.ui.YamcsUIPlugin;
import org.yamcs.studio.ui.connections.ConnectionStateProvider;
import org.yamcs.studio.ui.processor.ProcessorStateProvider;
import org.yamcs.utils.TimeEncoding;

public class ArchiveView extends ViewPart
        implements StudioConnectionListener, TimeListener, ISourceProviderListener, ConnectionListener {

    private static final Logger log = Logger.getLogger(ArchiveView.class.getName());

    ArchiveIndexReceiver indexReceiver;
    public ArchivePanel archivePanel;
    YamcsConnector yconnector;
    private String instance;
    private ResourceManager resourceManager;

    private Label replayTimeLabel;
    private Composite replayComposite;
    private GridData replayCompositeGridData;

    private Image playImage;
    private Image pauseImage;
    private Image forwardImage;
    private Image forward2xImage;
    private Image forward4xImage;
    private Image forward8xImage;
    private Image forward16xImage;
    private Image leaveReplayImage;

    private Button playButton;
    private Button forwardButton;
    private Button leaveReplayButton;

    @SuppressWarnings("rawtypes")
    private Map combinedState = new HashMap();

    @Override
    public void createPartControl(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        playImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/play.png"));
        pauseImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/pause.png"));
        forwardImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward.png"));
        forward2xImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward2x.png"));
        forward4xImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward4x.png"));
        forward8xImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward8x.png"));
        forward16xImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/forward16x.png"));
        leaveReplayImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/redo.png"));

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
        gl = new GridLayout(3, false);
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
        gd.widthHint = 140;
        replayTimeLabel.setLayoutData(gd);

        Composite controlsComposite = new Composite(replayComposite, SWT.NONE);
        gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        gd.grabExcessHorizontalSpace = true;
        controlsComposite.setLayoutData(gd);
        gl = new GridLayout(5, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        controlsComposite.setLayout(gl);

        playButton = new Button(controlsComposite, SWT.PUSH);
        playButton.setImage(playImage);
        playButton.setToolTipText("Play");
        playButton.addListener(SWT.Selection, evt -> {
            if (playButton.getImage().equals(playImage))
                RCPUtils.runCommand("org.yamcs.studio.ui.processor.playCommand");
            else
                RCPUtils.runCommand("org.yamcs.studio.ui.processor.pauseCommand");
        });

        forwardButton = new Button(controlsComposite, SWT.PUSH);
        forwardButton.setImage(forwardImage);
        forwardButton.setToolTipText("Forward");
        forwardButton.addListener(SWT.Selection, evt -> {
            RCPUtils.runCommand("org.yamcs.studio.ui.processor.forwardCommand");
        });

        Composite buttonWrapper = new Composite(replayComposite, SWT.NONE);
        gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        gd.widthHint = 140;
        buttonWrapper.setLayoutData(gd);

        gl = new GridLayout();
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        buttonWrapper.setLayout(gl);

        leaveReplayButton = new Button(buttonWrapper, SWT.PUSH);
        leaveReplayButton.setImage(leaveReplayImage);
        leaveReplayButton.setToolTipText("Back to Realtime");
        leaveReplayButton.addListener(SWT.Selection, evt -> {
            RCPUtils.runCommand("org.yamcs.studio.ui.processor.leaveReplay");
        });
        gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        buttonWrapper.setLayoutData(gd);

        toggleReplayComposite(false);

        ISourceProviderService service = (ISourceProviderService) getSite().getService(ISourceProviderService.class);
        ProcessorStateProvider processorState = (ProcessorStateProvider) service.getSourceProvider(ProcessorStateProvider.STATE_KEY_PROCESSING);
        processorState.addSourceProviderListener(this);
        ConnectionStateProvider connectionState = (ConnectionStateProvider) service.getSourceProvider(ConnectionStateProvider.STATE_KEY_CONNECTED);
        connectionState.addSourceProviderListener(this);

        indexReceiver.setIndexListener(this);
        yconnector.addConnectionListener(this);
        TimeCatalogue.getInstance().addTimeListener(this);
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
    public void onStudioConnect(YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, WebSocketRegistrar webSocketClient) {
        yconnector.connect(hornetqProps);
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
    public void processTime(long missionTime) {
        SwingUtilities.invokeLater(() -> {
            archivePanel.getDataViewer().getDataView().setCurrentLocator(missionTime);
        });
        Display.getDefault().asyncExec(() -> {
            if (replayTimeLabel.isDisposed())
                return;

            if (missionTime == TimeEncoding.INVALID_INSTANT || missionTime == 0) {
                replayTimeLabel.setText("");
            } else {
                String prettyTime = TimeCatalogue.getInstance().toString(missionTime);
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
        Float replaySpeed = (Float) combinedState.get(ProcessorStateProvider.STATE_KEY_REPLAY_SPEED);
        if (connected == null || processing == null || replay == null || replaySpeed == null)
            return;

        toggleReplayComposite(replay);

        boolean playEnabled = (Boolean.TRUE.equals(connected));
        playEnabled &= ("PAUSED".equals(processing));
        playEnabled &= (Boolean.TRUE.equals(replay));

        boolean pauseEnabled = (Boolean.TRUE.equals(connected));
        pauseEnabled &= ("RUNNING".equals(processing));
        pauseEnabled &= (Boolean.TRUE.equals(replay));

        boolean forwardEnabled = (Boolean.TRUE.equals(connected));
        forwardEnabled &= ("RUNNING".equals(processing));
        forwardEnabled &= (Boolean.TRUE.equals(replay));

        boolean leaveReplayEnabled = (Boolean.TRUE.equals(connected));

        playButton.setEnabled(playEnabled || pauseEnabled);
        forwardButton.setEnabled(forwardEnabled);
        leaveReplayButton.setEnabled(leaveReplayEnabled);

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

        if (floatEquals(replaySpeed, 1)) {
            forwardButton.setImage(forwardImage);
        } else if (floatEquals(replaySpeed, 2)) {
            forwardButton.setImage(forward2xImage);
        } else if (floatEquals(replaySpeed, 4)) {
            forwardButton.setImage(forward4xImage);
        } else if (floatEquals(replaySpeed, 8)) {
            forwardButton.setImage(forward8xImage);
        } else if (floatEquals(replaySpeed, 16)) {
            forwardButton.setImage(forward16xImage);
        } else {
            // TODO should draw the speed on top of the button
            log.warning("Unsupported speed " + replaySpeed);
            forwardButton.setImage(forwardImage);
        }
    }

    private static boolean floatEquals(float f1, float f2) {
        return (f1 == f2) ? true : Math.abs(f1 - f2) < 0.00001;
    }
}
