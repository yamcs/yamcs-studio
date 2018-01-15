/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author carcassi
 */
class ScannerParameters {

    enum Type {ACTIVE, PASSIVE}

    private Type type = Type.ACTIVE;
    private ScheduledExecutorService scannerExecutor;
    private Duration maxDuration;
    private Duration timeout;
    private String timeoutMessage;
    private DesiredRateEventListener listener;

    public ScannerParameters type(Type type) {
        this.type = type;
        return this;
    }

    public Type getType() {
        return type;
    }

    public ScannerParameters maxDuration(Duration maxDuration) {
        this.maxDuration = maxDuration;
        return this;
    }

    public Duration getMaxDuration() {
        return maxDuration;
    }

    public ScannerParameters readerDirector(PVDirector readerDirector) {
        this.listener = readerDirector.getDesiredRateEventListener();
        return this;
    }

    public ScannerParameters writerDirector(PVWriterDirector director) {
        this.listener = director.getDesiredRateEventListener();
        return this;
    }

    public ScannerParameters scannerExecutor(ScheduledExecutorService scannerExecutor) {
        this.scannerExecutor = scannerExecutor;
        return this;
    }

    public ScheduledExecutorService getScannerExecutor() {
        return scannerExecutor;
    }

    public ScannerParameters timeout(Duration timeout, String timeoutMessage) {
        this.timeout = timeout;
        this.timeoutMessage = timeoutMessage;
        return this;
    }

    public SourceDesiredRateDecoupler build() {
        if (type == Type.ACTIVE) {
            if (scannerExecutor == null) {
                throw new NullPointerException("Active scanner requires a scannerExecutor");
            }
            if (listener == null) {
                throw new NullPointerException("Active scanner requires a director");
            }
            if (maxDuration == null) {
                throw new NullPointerException("Active scanner requires a maxDuration");
            }
            return new ActiveScanDecoupler(scannerExecutor, maxDuration, listener);
        }
        if (type == Type.PASSIVE) {
            if (scannerExecutor == null) {
                throw new NullPointerException("Passive scanner requires a scannerExecutor");
            }
            if (listener == null) {
                throw new NullPointerException("Passive scanner requires a director");
            }
            if (maxDuration == null) {
                throw new NullPointerException("Passive scanner requires a maxDuration");
            }
            return new PassiveScanDecoupler(scannerExecutor, maxDuration, listener);
        }
        throw new IllegalStateException("Can't create suitable scanner");
    }

}
