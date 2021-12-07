/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui;

import java.io.BufferedInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

/**
 * Responsible for playing sound. Currently supports just one loop track which can be turned on or off, and a beep
 */
public class SoundSystem {

    private static final Logger log = Logger.getLogger(SoundSystem.class.getName());
    private static final String ALARM_SOUND = "/sounds/alarm.wav";
    private static final String BEEP_SOUND = "/sounds/beep.wav";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private boolean alarmState = false;
    private boolean muted = false;
    private Clip alarmClip;

    private AtomicBoolean beepRunning = new AtomicBoolean(false);

    public void mute() {
        executorService.execute(() -> {
            muted = true;
            if (alarmState) {
                playAlarm();
            }
        });
    }

    public void unmute() {
        executorService.execute(() -> {
            muted = false;
            stopAlarmSound();
        });
    }

    /**
     * Plays a beep, unless there is already a clip playing.
     */
    public void beep() {
        if (beepRunning.compareAndSet(false, true)) {
            try {
                var beepClip = AudioSystem.getClip();
                var in = SoundSystem.class.getResourceAsStream(BEEP_SOUND);
                var audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
                beepClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        beepClip.close();
                        beepRunning.set(false);
                    }
                });
                beepClip.open(audioIn);
                beepClip.start();
            } catch (Exception e) {
                beepRunning.set(false);
            }
        }
    }

    /**
     * Starts the alarm sound if the system is not muted. Otherwise will start the sound whenever the system is unmuted.
     */
    public void startAlarmSound() {
        executorService.execute(() -> {
            if (alarmState) {
                return; // Already on
            }

            alarmState = true;
            log.fine("Alarm ON");

            if (!muted) {
                playAlarm();
            }
        });
    }

    /**
     * Stops the alarm sound, if it wasn't off already
     */
    public void stopAlarmSound() {
        executorService.execute(() -> {
            alarmState = false;
            stopAlarm();
        });
    }

    private void playAlarm() {
        try {
            alarmClip = AudioSystem.getClip();
            var in = SoundSystem.class.getResourceAsStream(ALARM_SOUND);
            var audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
            alarmClip.open(audioIn);
            alarmClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            log.log(Level.FINE, "Error playing alarm sound", e);
        }
    }

    private void stopAlarm() {
        if (alarmClip != null) {
            log.fine("Alarm OFF");
            alarmClip.close();
            alarmClip = null;
        }
    }

    public void dispose() {
        if (alarmClip != null) {
            alarmClip.close();
        }
        executorService.shutdown();
    }
}
