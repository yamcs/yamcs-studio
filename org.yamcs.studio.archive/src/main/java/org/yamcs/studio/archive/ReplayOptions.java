package org.yamcs.studio.archive;

import java.time.ZoneOffset;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.utils.RCPUtils;

public class ReplayOptions extends Composite {

    private Image playImage;
    private Image pauseImage;
    private Image leaveReplayImage;

    private Button seekButton;
    private Button playButton;
    private Button leaveReplayButton;

    private Combo speedCombo;

    public ReplayOptions(Composite parent, ArchiveView archiveView) {
        super(parent, SWT.NONE);

        var resourceManager = new LocalResourceManager(JFaceResources.getResources(), this);
        playImage = resourceManager.createImage(RCPUtils.getImageDescriptor(ArchiveView.class, "icons/play.png"));
        pauseImage = resourceManager.createImage(RCPUtils.getImageDescriptor(ArchiveView.class, "icons/pause.png"));
        leaveReplayImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(ArchiveView.class, "icons/redo.png"));

        var gl = new GridLayout(4, false);
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        setLayout(gl);

        // current time / jump date / jump time / jump button
        var timeComposite = new Composite(this, SWT.NONE);
        var gd = new GridData();
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
            var dialog = new JumpToDialog(parent.getShell());
            if (dialog.open() == Dialog.OK) {
                var newPosition = dialog.getTime().atOffset(ZoneOffset.UTC);
                archiveView.seekReplay(newPosition);
            }
        });

        // play / pause / forward
        var controlsComposite = new Composite(this, SWT.NONE);
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

        var label = new Label(controlsComposite, SWT.PUSH);
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

        var buttonWrapper = new Composite(this, SWT.NONE);
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
    }

    private void updateSpeed() {
        var text = speedCombo.getText().trim();
        var speedFactor = -1f;
        if ("1x (original)".equals(text)) {
            speedFactor = 1;
        } else {
            speedFactor = Float.parseFloat(text.replace("x", ""));
        }
        if (speedFactor > 0) {
            speedCombo.setText(speedFactor + "x");
            var processor = YamcsPlugin.getProcessorClient();
            processor.changeSpeed(speedFactor + "x");
        }
    }

    void updateState(boolean connected, String processing, boolean replay, float replaySpeed) {
        boolean playEnabled = (Boolean.TRUE.equals(connected));
        playEnabled &= ("PAUSED".equals(processing));
        playEnabled &= (Boolean.TRUE.equals(replay));

        boolean pauseEnabled = (Boolean.TRUE.equals(connected));
        pauseEnabled &= ("RUNNING".equals(processing));
        pauseEnabled &= (Boolean.TRUE.equals(replay));

        boolean speedEnabled = (Boolean.TRUE.equals(connected));
        speedEnabled &= replaySpeed > 0;

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
