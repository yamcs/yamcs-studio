/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.css.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.yamcs.YamcsSubscriptionService;

public class Activator extends AbstractUIPlugin {

    private static BundleContext bundleContext;
    private static Activator plugin;

    private Beeper beeper;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        plugin = this;
        beeper = new Beeper();
        var service = YamcsPlugin.getService(YamcsSubscriptionService.class);
        service.addParameterValueListener(beeper);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public Beeper getBeeper() {
        return beeper;
    }
}
