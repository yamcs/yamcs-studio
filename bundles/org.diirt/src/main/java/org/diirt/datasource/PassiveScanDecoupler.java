/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author carcassi
 */
class PassiveScanDecoupler extends SourceDesiredRateDecoupler {

    private static final Logger log = Logger.getLogger(PassiveScanDecoupler.class.getName());
    // TODO: this could be made configurable between FINEST and OFF, and the if
    // modified so that code elimination would remove the logging completely
    private static final Level logLevel = Level.FINEST;

    private DesiredRateEvent queuedEvent;
    private Instant lastSubmission;
    private boolean scanActive;

    public PassiveScanDecoupler(ScheduledExecutorService scannerExecutor,
            Duration maxDuration, DesiredRateEventListener listener) {
        super(scannerExecutor, maxDuration, listener);
        synchronized(lock) {
            lastSubmission = Instant.now().minus(getMaxDuration());
        }
    }

    private final Runnable notificationTask = new Runnable() {

        @Override
        public void run() {
            DesiredRateEvent nextEvent;
            synchronized(lock) {
                nextEvent = queuedEvent;
                queuedEvent = null;
                if (log.isLoggable(logLevel)) {
                    log.log(logLevel, "Submitted {0}", Instant.now());
                }
            }

            // If stopped, the event may be null. Skip the event.
            if (nextEvent != null) {
                sendDesiredRateEvent(nextEvent);
            } else {
                log.log(logLevel, "Skipping null event {0}", Instant.now());
            }
        }
    };

    @Override
    void onStart() {
        // When starting, send an event in case the expressions
        // are constants
        newEvent(DesiredRateEvent.Type.READ_CONNECTION);
    }

    @Override
    void onStop() {
        synchronized(lock) {
            queuedEvent = null;
        }
    }

    @Override
    void onResume() {
        onDesiredEventProcessed();
    }

    @Override
    void newReadConnectionEvent() {
        newEvent(DesiredRateEvent.Type.READ_CONNECTION);
    }

    @Override
    void newWriteConnectionEvent() {
        newEvent(DesiredRateEvent.Type.WRITE_CONNECTION);
    }

    @Override
    void newValueEvent() {
        newEvent(DesiredRateEvent.Type.VALUE);
    }

    @Override
    void newReadExceptionEvent() {
        newEvent(DesiredRateEvent.Type.READ_EXCEPTION);
    }

    @Override
    void newWriteExceptionEvent() {
        newEvent(DesiredRateEvent.Type.WRITE_EXCEPTION);
    }

    @Override
    void newWriteSuccededEvent() {
        DesiredRateEvent event = new DesiredRateEvent();
        event.addType(DesiredRateEvent.Type.WRITE_SUCCEEDED);
        scheduleWriteOutcome(event);
    }

    @Override
    void newWriteFailedEvent(Exception ex) {
        DesiredRateEvent event = new DesiredRateEvent();
        event.addWriteFailed(new RuntimeException());
        sendDesiredRateEvent(event);
    }

    @Override
    void onDesiredEventProcessed() {
        Duration delay = null;
        synchronized (lock) {
            // If an event is pending submit it
            if (queuedEvent != null) {
                Instant nextSubmission = lastSubmission.plus(getMaxDuration());
                delay = Duration.between(Instant.now(), nextSubmission);
                if (!delay.isNegative() || !delay.isZero()) {
                    lastSubmission = nextSubmission;
                    if (log.isLoggable(logLevel)) {
                        log.log(logLevel, "Schedule next {0}", Instant.now());
                    }
                } else {
                    lastSubmission = Instant.now();
                    if (log.isLoggable(logLevel)) {
                        log.log(logLevel, "Schedule now {0}", Instant.now());
                    }
                }
            } else {
                scanActive = false;
                if (log.isLoggable(logLevel)) {
                    log.log(logLevel, "Do not schedule next {0}", Instant.now());
                }
            }
        }

        if (delay != null) {
            scheduleNext(delay);
        }
    }

    private void newEvent(DesiredRateEvent.Type type) {
        boolean submit;
        Duration delay = null;

        synchronized (lock) {
            // Add event to the queue
            if (queuedEvent == null) {
                queuedEvent = new DesiredRateEvent();
                if (log.isLoggable(logLevel)) {
                    log.log(logLevel, "Creating queued event {0}", Instant.now());
                }
            }
            queuedEvent.addType(type);

            // If scan is not active, submit the next scan
            if (!scanActive && !isPaused()) {
                submit = true;
                Instant currentInstant = Instant.now();
                Instant nextTimeSlot = lastSubmission.plus(getMaxDuration());
                if (currentInstant.compareTo(nextTimeSlot) < 0) {
                    delay = Duration.between(currentInstant, nextTimeSlot);
                    lastSubmission = nextTimeSlot;
                    if (log.isLoggable(logLevel)) {
                        log.log(logLevel, "Submit delayed {0}", Instant.now());
                    }
                } else {
                    lastSubmission = currentInstant;
                    if (log.isLoggable(logLevel)) {
                        log.log(logLevel, "Submit now {0}", Instant.now());
                    }
                }
                scanActive = true;
            } else {
                submit = false;
                if (log.isLoggable(logLevel)) {
                    log.log(logLevel, "Do not submit {0}", Instant.now());
                }
            }

        }
        if (submit) {
            scheduleNext(delay);
        }
    }

    private void scheduleNext(Duration delay) {
        if (delay == null || delay.isNegative()) {
            getScannerExecutor().submit(notificationTask);
        } else {
            getScannerExecutor().schedule(notificationTask, delay.toNanos(), TimeUnit.NANOSECONDS);
        }
    }



    /**
     * If possible, submit the event right away, otherwise try again later.
     * @param event the event to submit
     */
    private void scheduleWriteOutcome(final DesiredRateEvent event) {
        if (!isEventProcessing()) {
            sendDesiredRateEvent(event);
        } else {
            getScannerExecutor().submit(new Runnable() {

                @Override
                public void run() {
                    scheduleWriteOutcome(event);
                }
            });
        }
    }

}
