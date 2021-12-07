/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PVFactory {

    /**
     * The default background thread for PV change event notification. It will only be created on its first use.
     */
    public static ExecutorService SIMPLE_PV_THREAD = null;

    private static final PVFactory INSTANCE = new PVFactory();

    private List<Datasource> datasources = new ArrayList<>();

    private PVFactory() {
        datasources.add(new FormulaDatasource()); // =
        datasources.add(new LocalDatasource()); // loc://
        datasources.add(new SimDatasource()); // sim://
        datasources.add(new StateDatasource()); // state://
        datasources.add(new SysDatasource()); // sys://
        datasources.add(new ParameterDatasource()); // Keep last (it's the default)
    }

    public static PVFactory getInstance() {
        return INSTANCE;
    }

    public synchronized IPV createPV(String name) {
        if (SIMPLE_PV_THREAD == null) {
            SIMPLE_PV_THREAD = Executors.newSingleThreadExecutor();
        }
        return createPV(name, false, SIMPLE_PV_THREAD, null);
    }

    /**
     * Create a PV.
     *
     * @param name
     *            name of the PV. Must not be null.
     * @param readOnly
     *            true if the client doesn't need to write to the PV.
     * @param notificationThread
     *            the thread on which the read and write listener will be notified. Must not be null.
     * @param exceptionHandler
     *            the handler to handle all exceptions happened in pv connection layer. If this is null, pv read
     *            listener or pv write listener will be notified on read or write exceptions respectively.
     */
    public IPV createPV(String name, boolean readOnly, Executor notificationThread, ExceptionHandler exceptionHandler) {
        Datasource datasource = null;
        for (Datasource candidate : datasources) {
            if (candidate.supportsPVName(name)) {
                datasource = candidate;
                break;
            }
        }
        return new IPV(name, datasource, notificationThread);
    }

    public static synchronized ExecutorService getDefaultPVNotificationThread() {
        if (SIMPLE_PV_THREAD == null) {
            SIMPLE_PV_THREAD = Executors.newSingleThreadExecutor();
        }
        return SIMPLE_PV_THREAD;
    }
}
