/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.util;

import java.io.InputStream;

/**
 * A runnable that able to inject inputstream to the task, so the method {@link #setInputStream(InputStream)} must be
 * called before scheduling this task. Subclass should only implement {@link #runWithInputStream(InputStream)}.
 */
public abstract class AbstractInputStreamRunnable implements Runnable {

    private InputStream inputStream;

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * The task to be executed.
     * 
     * @param inputStream
     *            the injected inputstream.
     */
    public abstract void runWithInputStream(InputStream inputStream);

    @Override
    public void run() {
        runWithInputStream(inputStream);
    }

}
