package org.yamcs.studio.core.ui;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * Responsible for playing sound. Currently supports just one loop track which
 * can be turned on or off, and a beep
 */
public class SoundSystem {

    private static final Logger log = Logger.getLogger(SoundSystem.class
            .getName());
    private static final String ALARM_SOUND = "/sounds/alarm.wav";
    private static final String BEEP_SOUND = "/sounds/beep.wav";

    private ExecutorService executorService = Executors
            .newSingleThreadExecutor();

    private boolean alarmState = false;
    private boolean muted = false;
    private Clip alarmClip;

    public void mute() {
        executorService.execute(() -> {
            muted = true;
            if (alarmState)
                playAlarm();
        });
    }

    public void unmute() {
        executorService.execute(() -> {
            muted = false;
            stopAlarmSound();
        });
    }

    static Clip beepClip = null;

    /**
     * Plays a beep, only if there is not currently a previous beep playing.
     */
    synchronized public static void beep() {
        try {
            if (beepClip != null) {
                return;
            }
            beepClip = AudioSystem.getClip();
            InputStream in = SoundSystem.class.getResourceAsStream(BEEP_SOUND);
            AudioInputStream audioIn = AudioSystem
                    .getAudioInputStream(new BufferedInputStream(in));
            beepClip.open(audioIn);
            beepClip.start();

            // explicitly stop the clip when sound has stopped
            beepClip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        event.getLine().close();
                        beepClip = null;
                    }
                }
            });

        } catch (Exception e) {
            System.out.println(e);
            log.log(Level.WARNING, "Error playing beep sound", e);
        }
    }

    /**
     * Starts the alarm sound if the system is not muted. Otherwise will start
     * the sound whenever the system is unmuted.
     */
    public void startAlarmSound() {
        executorService.execute(() -> {
            if (alarmState)
                return; // Already on

            alarmState = true;
            log.fine("Alarm ON");

            if (!muted)
                playAlarm();
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
            InputStream in = SoundSystem.class.getResourceAsStream(ALARM_SOUND);
            AudioInputStream audioIn = AudioSystem
                    .getAudioInputStream(new BufferedInputStream(in));
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
        executorService.shutdown();
    }

    public static void main(String[] arg) {
        System.out.println("playing test beeps");
        while (true) {
            SoundSystem.beep();
        }
    }
}
