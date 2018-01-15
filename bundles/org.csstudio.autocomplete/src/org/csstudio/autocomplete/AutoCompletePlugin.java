/*******************************************************************************
 * Copyright (c) 2010-2016 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.autocomplete;

import java.util.logging.Logger;

import org.csstudio.autocomplete.pvmanager.sim.DSFunctionRegistry;
import org.csstudio.autocomplete.pvmanager.sim.SimDSFunctionSet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Fred Arnaud (Sopra Group) - ITER
 */
public class AutoCompletePlugin implements BundleActivator {

    public static final String PLUGIN_ID = "org.csstudio.autocomplete";

    public static final Logger logger = Logger.getLogger(PLUGIN_ID);

    private static BundleContext bundleContext;

    /** @return Logger for plugin ID */
    public static Logger getLogger() {
        return logger;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        DSFunctionRegistry.getDefault().registerDSFunctionSet(new SimDSFunctionSet());
    }

    @Override
    public void stop(BundleContext arg0) throws Exception {
    }
}
