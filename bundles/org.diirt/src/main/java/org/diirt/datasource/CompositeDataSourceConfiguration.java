/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

/**
 * Configuration for {@link CompositeDataSource}. This object is mutable, and therefore not thread-safe.
 *
 * @author carcassi
 */
public final class CompositeDataSourceConfiguration {

    // Package private so we don't need getters
    String delimiter = "://";
    String defaultDataSource;

    /**
     * Returns the delimeter that divides the data source name from the channel name. Default is "://" so that
     * "ca://pv1" corresponds to the "pv1" channel from the "ca" datasource.
     *
     * @return data source delimeter; can't be null
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Changes the data source delimiter.
     *
     * @param delimiter
     *            new data source delimiter; can't be null
     * @return this
     */
    public CompositeDataSourceConfiguration delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Returns which data source is used if no data source is specified in the channel name.
     *
     * @return the default data source, or null if it was never set
     */
    public String getDefaultDataSource() {
        return defaultDataSource;
    }

    /**
     * Sets the data source to be used if the channel does not specify one explicitely. The data source must have
     * already been added.
     *
     * @param defaultDataSource
     *            the default data source
     */
    public CompositeDataSourceConfiguration defaultDataSource(String defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
        return this;
    }

}
