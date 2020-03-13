package org.yamcs.studio.archive;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;
import org.yamcs.protobuf.EditProcessorRequest;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.model.TimeListener;
import org.yamcs.studio.core.ui.connections.ConnectionStateProvider;
import org.yamcs.studio.core.ui.processor.ProcessorStateProvider;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.utils.TimeEncoding;

public class ArchiveView extends ViewPart
        implements YamcsConnectionListener, InstanceListener, TimeListener, ISourceProviderListener {

    ArchiveIndexReceiver indexReceiver;
    public ArchivePanel archivePanel;

    private Composite replayComposite;
    private GridData replayCompositeGridData;

    private Image playImage;
    private Image pauseImage;
    private Image leaveReplayImage;

    private Button seekButton;
    private Button playButton;
    private Button leaveReplayButton;

    private Combo speedCombo;

    private ProcessorStateProvider processorState;
    private ConnectionStateProvider connectionState;

    private java.awt.Frame frame;
    private ComponentListener frameListener;

    @Override
    public void createPartControl(Composite parent) {
        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        playImage = resourceManager.createImage(RCPUtils.getImageDescriptor(ArchiveView.class, "icons/play.png"));
        pauseImage = resourceManager.createImage(RCPUtils.getImageDescriptor(ArchiveView.class, "icons/pause.png"));
        leaveReplayImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(ArchiveView.class, "icons/redo.png"));

        createActions();

        indexReceiver = new ArchiveIndexReceiver();

        parent.setLayout(new FillLayout());

        Composite contentArea = new Composite(parent, SWT.NONE);

        contentArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        contentArea.setLayout(gl);

        Composite locationComp = new Composite(contentArea, SWT.EMBEDDED);
        locationComp.setLayoutData(new GridData(GridData.FILL_BOTH));
        frame = SWT_AWT.new_Frame(locationComp);

        archivePanel = new ArchivePanel(this);
        archivePanel.setPreferredSize(new Dimension(300, 400));

        frameListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                archivePanel.onWindowResized();
            }
        };
        frame.addComponentListener(frameListener);
        frame.add(archivePanel);

        replayComposite = new Composite(contentArea, SWT.NONE);
        replayCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
        replayComposite.setLayoutData(replayCompositeGridData);
        gl = new GridLayout(4, false);
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        replayComposite.setLayout(gl);

        // current time / jump date / jump time / jump button
        Composite timeComposite = new Composite(replayComposite, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.LEFT;
        // gd.widthHint = 140;
        // gd.grabExcessHorizontalSpace = true;
        timeComposite.setLayoutData(gd);
        gl = new GridLayout(4, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        timeComposite.setLayout(gl);

        seekButton = new Button(timeComposite, SWT.PUSH);
        seekButton.setText("Jump to...");
        seekButton.setToolTipText("Jump to Specific Time");
        seekButton.addListener(SWT.Selection, evt -> {
            JumpToDialog dialog = new JumpToDialog(parent.getShell());
            if (dialog.open() == Dialog.OK) {
                long seekInstant = TimeEncoding.fromCalendar(dialog.getTime());
                archivePanel.seekReplay(seekInstant);
            }
        });

        // play / pause / forward
        Composite controlsComposite = new Composite(replayComposite, SWT.NONE);
        gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        gd.grabExcessHorizontalSpace = true;
        controlsComposite.setLayoutData(gd);
        gl = new GridLayout(4, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        controlsComposite.setLayout(gl);

        playButton = new Button(controlsComposite, SWT.PUSH);
        playButton.setImage(playImage);
        playButton.setToolTipText("Play");
        playButton.addListener(SWT.Selection, evt -> {
            if (playButton.getImage().equals(playImage)) {
                RCPUtils.runCommand("org.yamcs.studio.core.ui.processor.playCommand");
            } else {
                RCPUtils.runCommand("org.yamcs.studio.core.ui.processor.pauseCommand");
            }
        });

        Label label = new Label(controlsComposite, SWT.PUSH);
        label.setText("Speed:");
        speedCombo = new Combo(controlsComposite, SWT.DROP_DOWN);
        speedCombo.setItems("0.5x", "0.75x", "1x (original)", "2x", "5x", "10x", "20x");
        speedCombo.setText("1x (original)");

        speedCombo.addListener(SWT.Selection, evt -> updateSpeed());
        speedCombo.addListener(SWT.KeyUp, evt -> {
            if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) { // Enter
                updateSpeed();
            }
        });
        speedCombo.addListener(SWT.FocusOut, evt -> updateSpeed());

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
        leaveReplayButton.setToolTipText("Leave Replay");
        leaveReplayButton.addListener(SWT.Selection, evt -> {
            RCPUtils.runCommand("org.yamcs.studio.core.ui.processor.leaveReplay");
        });
        gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        buttonWrapper.setLayoutData(gd);

        ISourceProviderService service = (ISourceProviderService) getSite().getService(ISourceProviderService.class);
        processorState = (ProcessorStateProvider) service
                .getSourceProvider(ProcessorStateProvider.STATE_KEY_PROCESSING);
        processorState.addSourceProviderListener(this);
        connectionState = (ConnectionStateProvider) service
                .getSourceProvider(ConnectionStateProvider.STATE_KEY_CONNECTED);
        connectionState.addSourceProviderListener(this);

        indexReceiver.setIndexListener(this);
        TimeCatalogue.getInstance().addTimeListener(this);
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);

        updateState();
    }

    private void updateSpeed() {
        String text = speedCombo.getText().trim();
        float speedFactor = -1;
        if ("1x (original)".equals(text)) {
            speedFactor = 1;
        } else {
            speedFactor = Float.parseFloat(text.replace("x", ""));
        }
        if (speedFactor > 0) {
            speedCombo.setText(speedFactor + "x");

            ProcessorInfo processor = ManagementCatalogue.getInstance().getCurrentProcessorInfo();
            EditProcessorRequest req = EditProcessorRequest.newBuilder().setSpeed(speedFactor + "x").build();
            ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
            catalogue.editProcessorRequest(processor.getInstance(), processor.getName(), req);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        archivePanel.dispose();

        ManagementCatalogue.getInstance().removeInstanceListener(this);
        TimeCatalogue.getInstance().removeTimeListener(this);
        processorState.removeSourceProviderListener(this);
        connectionState.removeSourceProviderListener(this);
        YamcsPlugin.getDefault().removeYamcsConnectionListener(this);

        if (frame != null) {
            SwingUtilities.invokeLater(() -> {
                frame.removeComponentListener(frameListener);
                frame.removeAll();
            });
        }
    }

    private void toggleReplayComposite(boolean enabled) {
        replayCompositeGridData.exclude = !enabled;
        replayComposite.setVisible(enabled);
        replayComposite.getParent().layout();
    }

    @Override
    public void onYamcsConnected() {
        SwingUtilities.invokeLater(() -> {
            setRefreshEnabled(true);
            refreshData();
        });
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        clearInstanceSpecificState();
    }

    @Override
    public void onYamcsDisconnected() {
        clearInstanceSpecificState();
    }

    private void clearInstanceSpecificState() {
        SwingUtilities.invokeLater(() -> {
            setRefreshEnabled(false);
            archivePanel.getDataViewer().getDataView()
                    .setCurrentLocator(archivePanel.getDataViewer().getDataView().DO_NOT_DRAW);
            archivePanel.getDataViewer().clearView();
            refreshData();
        });
    }

    @Override
    public void processTime(long missionTime) {
        SwingUtilities.invokeLater(() -> {
            archivePanel.getDataViewer().getDataView().setCurrentLocator(missionTime);
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
                        if (dialog.hasStartTime()) {
                            range.setStart(dialog.getStartTime());
                        }
                        if (dialog.hasStopTime()) {
                            range.setStop(dialog.getStopTime());
                        }
                        doFilter(range);
                    }
                }
            }
        });
    }

    private void doFilter(TimeInterval range) {
        SwingUtilities.invokeLater(() -> {
            archivePanel.prefs.saveRange(range);
            refreshData();
        });
    }

    public boolean isRefreshEnabled() {
        // Not necessarily on the SWT thread. This is a bit of a risk. Maybe we should do a blocking
        // Display.getDefault().syncExec
        IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
        ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
        RefreshStateProvider commandState = (RefreshStateProvider) service
                .getSourceProvider(RefreshStateProvider.STATE_KEY_ENABLED);
        return (Boolean) commandState.getCurrentState().get(RefreshStateProvider.STATE_KEY_ENABLED);
    }

    public void setRefreshEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            RefreshStateProvider commandState = (RefreshStateProvider) service
                    .getSourceProvider(RefreshStateProvider.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public void setZoomInEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            ZoomInStateProvider commandState = (ZoomInStateProvider) service
                    .getSourceProvider(ZoomInStateProvider.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public void setZoomOutEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            ZoomOutStateProvider commandState = (ZoomOutStateProvider) service
                    .getSourceProvider(ZoomOutStateProvider.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
    }

    public void setZoomClearEnabled(boolean enabled) {
        // Back to the SWT thread, to be sure
        Display.getDefault().asyncExec(() -> {
            IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
            ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            ClearZoomStateProvider commandState = (ClearZoomStateProvider) service
                    .getSourceProvider(ClearZoomStateProvider.STATE_KEY_ENABLED);
            commandState.setEnabled(enabled);
        });
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
        // JOptionPane.showMessageDialog(this, msg, getTitle(), JOptionPane.PLAIN_MESSAGE);
    }

    protected void showInfo(String msg) {
        System.out.println(msg);
        // JOptionPane.showMessageDialog(this, msg, getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    protected void showError(String msg) {
        System.out.println(msg);
        // JOptionPane.showMessageDialog(this, msg, getTitle(), JOptionPane.ERROR_MESSAGE);
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
        TimeInterval interval = archivePanel.getRequestedDataInterval();
        new Thread() { // FIXME new thread because currently RestClients syncs on the call thread causing deadlock
            @Override
            public void run() {
                indexReceiver.getTag(interval);
            }
        }.start();
    }

    public void receiveTagsFinished() {
        archivePanel.archiveLoadFinished();
    }

    public void refreshData() {
        archivePanel.startReloading();
        TimeInterval interval = archivePanel.getRequestedDataInterval();
        indexReceiver.getIndex(interval);
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

    @Override
    public void setFocus() {
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
        updateState();
    }

    @Override
    public void sourceChanged(int sourcePriority, String sourceName, Object sourceValue) {
        updateState();
    }

    /*
     * We use the state available in the workbench-level state provider, which may have a need in the future. This way
     * we avoid duplicating similar logic here.
     */
    private void updateState() {

        // Refresh the current states
        Boolean connected = (Boolean) connectionState.getCurrentState()
                .get(ConnectionStateProvider.STATE_KEY_CONNECTED);
        String processing = (String) processorState.getCurrentState().get(ProcessorStateProvider.STATE_KEY_PROCESSING);
        Boolean replay = (Boolean) processorState.getCurrentState().get(ProcessorStateProvider.STATE_KEY_REPLAY);
        Float replaySpeed = (Float) processorState.getCurrentState().get(ProcessorStateProvider.STATE_KEY_REPLAY_SPEED);

        if (connected == null || processing == null || replay == null || replaySpeed == null) {
            return;
        }

        toggleReplayComposite(replay);

        boolean playEnabled = (Boolean.TRUE.equals(connected));
        playEnabled &= ("PAUSED".equals(processing));
        playEnabled &= (Boolean.TRUE.equals(replay));

        boolean pauseEnabled = (Boolean.TRUE.equals(connected));
        pauseEnabled &= ("RUNNING".equals(processing));
        pauseEnabled &= (Boolean.TRUE.equals(replay));

        boolean speedEnabled = (Boolean.TRUE.equals(connected));
        speedEnabled &= replaySpeed != null && replaySpeed > 0;

        boolean leaveReplayEnabled = (Boolean.TRUE.equals(connected));

        playButton.setEnabled(playEnabled || pauseEnabled);
        leaveReplayButton.setEnabled(leaveReplayEnabled);
        speedCombo.setEnabled(speedEnabled);

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
