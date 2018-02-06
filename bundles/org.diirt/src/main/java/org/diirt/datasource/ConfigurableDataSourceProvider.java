/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.diirt.util.Configuration;

/**
 * DataSourceProvider for a data source that require configuration.
 *
 * @author carcassi
 * @param <D> the data source created by the provider
 * @param <C> the configuration loaded by the provider
 */
public abstract class ConfigurableDataSourceProvider<D extends DataSource, C extends DataSourceConfiguration<D>> extends DataSourceProvider {

    protected Class<C> clazz;

    /**
     * Create a new configurable data source provider.
     *
     * @param clazz the class token for the configuration object
     */
    protected ConfigurableDataSourceProvider(Class<C> clazz) {
        this.clazz = clazz;
    }

    /**
     * The path from DIIRT_HOME where to find the configuration file.
     * <p>
     * Default is "datasource/[providerName]".
     *
     * @return the path to the configuration directory for the data source
     */
    public String getConfigurationPath() {
        return "datasources/" + getName();
    }

    /**
     * The name of the configuration file.
     * <p>
     * Default is "[providerName].xml"
     *
     * @return the data source configuration filename
     */
    public String getConfigurationFilename() {
        return getName() + ".xml";
    }

    /**
     * The name of the bundled resource with the default configuration
     * file. This is used by the framework to initialize the configuration
     * file in the directory. It must be bundled in the same package of
     * the provider.
     *
     * @return
     */
    public String getBundledDefaultConfiguration() {
        return getName() + ".default.xml";
    }

    /**
     * Creates the instance by reading the configuration file and creating
     * the data source from the configuration.
     *
     * @return a new data source
     */
    @Override
    public D createInstance() {
        return createInstance(getConfigurationPath());
    }

    private D createInstance(String confPath) {
        C defaultConfiguration;
        try {
            defaultConfiguration = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Counding instanciate configuration object " + clazz.getSimpleName(), ex);
            return null;
        }

        DataSourceConfiguration<D> configuration = defaultConfiguration;
        try (InputStream input = Configuration.getFileAsStream(confPath + "/" + getConfigurationFilename(), this, getBundledDefaultConfiguration())) {
            configuration = defaultConfiguration.read(input);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Couldn't load DIIRT_HOME/" + confPath + "/file.xml", ex);
            return null;
        }
        return configuration.create();
    }
}
