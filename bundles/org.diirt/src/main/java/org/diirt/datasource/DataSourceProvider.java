/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

import java.util.logging.Logger;

import org.diirt.util.ServiceLoaderOSGiWrapper;

/**
 * A class that provides support for a DataSource.
 * <p>
 * This interface allows different modules to registers a DataSource through the ServiceLoader. Implementations that are
 * correctly registered will be asked to create a DataSource instance which will be registered into a
 * CompositeDataSource with the given name.
 * <p>
 * The factory only needs to care about the DataSource creation, and not the rest of the life-cycle.
 *
 * @author carcassi
 */
public abstract class DataSourceProvider {

    private static final Logger log = Logger.getLogger(DataSourceProvider.class.getName());

    /**
     * The name to be used when registering the DataSource with the CompositeDataSource.
     *
     * @return a short String
     */
    public abstract String getName();

    /**
     * Creates a new instance of the DataSource.
     *
     * @return a new DataSource
     */
    public abstract DataSource createInstance();

    /**
     * Looks up the registered factories and creates a CompositeDataSource using them.
     *
     * @return a new DataSource
     */
    public static CompositeDataSource createDataSource() {
        CompositeDataSource composite = new CompositeDataSource();
        composite.setConfiguration(new CompositeDataSourceConfiguration());
        ServiceLoaderOSGiWrapper.load(DataSourceProvider.class, log, composite::putDataSource);
        return composite;
    }
}
