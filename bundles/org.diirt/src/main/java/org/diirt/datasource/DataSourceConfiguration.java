/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

import java.io.InputStream;

/**
 * The configuration for a data source.
 * <p>
 * This class helps abstract out common functionality between data sources
 * related to managing the configuration (load/save from DIIRT_HOME, create
 * from the configuration).
 * <p>
 * All implementations must have a no-argument constructor, initialed to the
 * default configuration.
 *
 * @author carcassi
 * @param <T> the data source type to be configured
 */
public abstract class DataSourceConfiguration<T extends DataSource> {

    /**
     * Reads the configuration from the given stream and returns
     * an object with the new configuration.
     * <p>
     * NOTE: whether this object is modified and returned, or a new
     * object is returned, is currently unspecified.
     *
     * @param stream the configuration file to be read
     * @return the object with the new configuration
     */
    public abstract DataSourceConfiguration<T> read(InputStream stream);

    /**
     * Creates a new data source based on this configuration.
     *
     * @return a new data source
     */
    public abstract T create();
}
