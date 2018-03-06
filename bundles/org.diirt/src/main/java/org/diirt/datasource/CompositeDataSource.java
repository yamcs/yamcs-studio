/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A data source that can dispatch a request to multiple different
 * data sources.
 *
 * @author carcassi
 */
public class CompositeDataSource extends DataSource {

    private static final Logger log = Logger.getLogger(CompositeDataSource.class.getName());

    // Stores all data sources by name
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private final Map<String, DataSourceProvider> dataSourceProviders = new ConcurrentHashMap<>();

    private volatile CompositeDataSourceConfiguration conf = new CompositeDataSourceConfiguration();

    /**
     * Creates a new CompositeDataSource.
     */
    public CompositeDataSource() {
        super(true);
    }

    /**
     * The configuration used for the composite data source.
     *
     * @return the configuration; can't be null
     */
    public CompositeDataSourceConfiguration getConfiguration() {
        return conf;
    }

    /**
     * Changes the composite data source configuration.
     * <p>
     * NOTE: the configuration should be changed before any channel
     * is opened. The result of later changes is not well defined.
     *
     * @param conf the new configuration; can't be null
     */
    public void setConfiguration(CompositeDataSourceConfiguration conf) {
        if (conf == null) {
            throw new NullPointerException("Configuration can't be null.");
        }
        this.conf = conf;
    }

    /**
     * Adds/replaces the data source corresponding to the given name.
     *
     * @param name the name of the data source
     * @param dataSource the data source to add/replace
     */
    public void putDataSource(final String name, final DataSource dataSource) {
        putDataSource(new DataSourceProvider() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public DataSource createInstance() {
                return dataSource;
            }
        });
    }

    /**
     * Adds/replaces the data source corresponding to the given name.
     *
     * @param dataSourceProvider the data source to add/replace
     */
    public void putDataSource(DataSourceProvider dataSourceProvider) {
        // XXX: datasources should be closed
        dataSources.remove(dataSourceProvider.getName());
        dataSourceProviders.put(dataSourceProvider.getName(), dataSourceProvider);
    }

    /**
     * Returns the data sources used by this composite data source.
     * <p>
     * Returns only the data sources that have been created.
     *
     * @return the registered data sources
     */
    public Map<String, DataSource> getDataSources() {
        return Collections.unmodifiableMap(dataSources);
    }

    /**
     * Returns the data source providers registered to this composite data source.
     * <p>
     * Returns all registered data sources.
     *
     * @return the registered data source providers
     */
    public Map<String, DataSourceProvider> getDataSourceProviders() {
        return Collections.unmodifiableMap(dataSourceProviders);
    }

    private  String nameOf(String channelName) {
        String delimiter = conf.delimiter;
        int indexDelimiter = channelName.indexOf(delimiter);
        if (indexDelimiter == -1) {
            return channelName;
        } else {
            return channelName.substring(indexDelimiter + delimiter.length());
        }
    }

    private String sourceOf(String channelName) {
        String delimiter = conf.delimiter;
        String defaultDataSource = conf.defaultDataSource;
        int indexDelimiter = channelName.indexOf(delimiter);
        if (indexDelimiter == -1) {
            if (defaultDataSource == null)
                throw new IllegalArgumentException("Channel " + channelName + " uses default data source but one was never set.");
            if (!dataSourceProviders.containsKey(defaultDataSource))
                throw new IllegalArgumentException("Channel " + channelName + " uses default data source " + defaultDataSource + " which was not found.");
            return defaultDataSource;
        } else {
            String source = channelName.substring(0, indexDelimiter);
            if (dataSourceProviders.containsKey(source))
                return source;
            throw new IllegalArgumentException("Data source " + source + " for " + channelName + " was not configured.");
        }
    }

    private Map<String, ReadRecipe> splitRecipe(ReadRecipe readRecipe) {
        Map<String, ReadRecipe> splitRecipe = new HashMap<String, ReadRecipe>();

        // Iterate through the recipe to understand how to distribute
        // the calls
        Map<String, Collection<ChannelReadRecipe>> routingRecipes = new HashMap<String, Collection<ChannelReadRecipe>>();
        for (ChannelReadRecipe channelRecipe : readRecipe.getChannelReadRecipes()) {
            String name = nameOf(channelRecipe.getChannelName());
            String dataSource = sourceOf(channelRecipe.getChannelName());

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            // Add recipe for the target dataSource
            if (routingRecipes.get(dataSource) == null) {
                routingRecipes.put(dataSource, new HashSet<ChannelReadRecipe>());
            }
            routingRecipes.get(dataSource).add(new ChannelReadRecipe(name, channelRecipe.getReadSubscription()));
        }

        // Create the recipes
        for (Entry<String, Collection<ChannelReadRecipe>> entry : routingRecipes.entrySet()) {
            splitRecipe.put(entry.getKey(), new ReadRecipe(entry.getValue()));
        }

        return splitRecipe;
    }

    @Override
    public void connectRead(ReadRecipe readRecipe) {
        Map<String, ReadRecipe> splitRecipe = splitRecipe(readRecipe);

        // Dispatch calls to all the data sources
        for (Map.Entry<String, ReadRecipe> entry : splitRecipe.entrySet()) {
            try {
                retrieveDataSource(entry.getKey()).connectRead(entry.getValue());
            } catch (RuntimeException ex) {
                // If data source fail, still go and connect the others
                readRecipe.getChannelReadRecipes().iterator().next().getReadSubscription().getExceptionWriteFunction().writeValue(ex);
            }
        }
    }

    @Override
    public void disconnectRead(ReadRecipe readRecipe) {
        Map<String, ReadRecipe> splitRecipe = splitRecipe(readRecipe);

        // Dispatch calls to all the data sources
        for (Map.Entry<String, ReadRecipe> entry : splitRecipe.entrySet()) {
            try {
                dataSources.get(entry.getKey()).disconnectRead(entry.getValue());
            } catch(RuntimeException ex) {
                // If a data source fails, still go and disconnect the others
                readRecipe.getChannelReadRecipes().iterator().next().getReadSubscription().getExceptionWriteFunction().writeValue(ex);
            }
        }
    }

    private Map<String, WriteRecipe> splitRecipe(WriteRecipe writeRecipe) {
        // Chop the recipe along different data sources
        Map<String, Collection<ChannelWriteRecipe>> recipes = new HashMap<String, Collection<ChannelWriteRecipe>>();
        for (ChannelWriteRecipe channelWriteRecipe : writeRecipe.getChannelWriteRecipes()) {
            String channelName = nameOf(channelWriteRecipe.getChannelName());
            String dataSource = sourceOf(channelWriteRecipe.getChannelName());
            Collection<ChannelWriteRecipe> channelWriteRecipes = recipes.get(dataSource);
            if (channelWriteRecipes == null) {
                channelWriteRecipes = new ArrayList<ChannelWriteRecipe>();
                recipes.put(dataSource, channelWriteRecipes);
            }
            channelWriteRecipes.add(new ChannelWriteRecipe(channelName, channelWriteRecipe.getWriteSubscription()));
        }

        Map<String, WriteRecipe> splitRecipes = new HashMap<String, WriteRecipe>();
        for (Map.Entry<String, Collection<ChannelWriteRecipe>> en : recipes.entrySet()) {
            String dataSource = en.getKey();
            Collection<ChannelWriteRecipe> val = en.getValue();
            WriteRecipe newWriteRecipe = new WriteRecipe(val);
            splitRecipes.put(dataSource, newWriteRecipe);
        }

        return splitRecipes;
    }

    private DataSource retrieveDataSource(String name) {
        DataSource dataSource = dataSources.get(name);
        if (dataSource == null) {
            DataSourceProvider factory = dataSourceProviders.get(name);
            if (factory == null) {
                throw new IllegalArgumentException("DataSource '" + name + conf.delimiter + "' was not configured.");
            } else {
                dataSource = factory.createInstance();
                if (dataSource == null) {
                    throw new IllegalStateException("DataSourceProvider '" + name + conf.delimiter + "' did not create a valid datasource.");
                }
                dataSources.put(name, dataSource);
                log.log(Level.CONFIG, "Created instance for data source {0} ({1})", new Object[]{name, dataSource.getClass().getSimpleName()});
            }
        }
        return dataSource;
    }

    @Override
    public void connectWrite(WriteRecipe writeRecipe) {
        Map<String, WriteRecipe> splitRecipes = splitRecipe(writeRecipe);
        for (Entry<String, WriteRecipe> entry : splitRecipes.entrySet()) {
            String dataSource = entry.getKey();
            WriteRecipe splitWriteRecipe = entry.getValue();
            retrieveDataSource(dataSource).connectWrite(splitWriteRecipe);
        }
    }

    @Override
    public void disconnectWrite(WriteRecipe writeRecipe) {
        Map<String, WriteRecipe> splitRecipe = splitRecipe(writeRecipe);

        for (Map.Entry<String, WriteRecipe> en : splitRecipe.entrySet()) {
            String dataSource = en.getKey();
            WriteRecipe splitWriteRecipe = en.getValue();
            dataSources.get(dataSource).disconnectWrite(splitWriteRecipe);
        }
    }


    @Override
    ChannelHandler channel(String channelName) {
        String name = nameOf(channelName);
        String dataSource = sourceOf(channelName);
        return dataSources.get(dataSource).channel(name);
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        throw new UnsupportedOperationException("Composite data source can't create channels directly.");
    }

    /**
     * Closes all DataSources that are registered in the composite.
     */
    @Override
    public void close() {
        for (DataSource dataSource : dataSources.values()) {
            dataSource.close();
        }
    }

    @Override
    public Map<String, ChannelHandler> getChannels() {
        Map<String, ChannelHandler> channels = new HashMap<String, ChannelHandler>();
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            String dataSourceName = entry.getKey();
            DataSource dataSource = entry.getValue();
            for (Entry<String, ChannelHandler> channelEntry : dataSource.getChannels().entrySet()) {
                String channelName = channelEntry.getKey();
                ChannelHandler channelHandler = channelEntry.getValue();
                channels.put(dataSourceName + conf.delimiter + channelName, channelHandler);
            }
        }

        return channels;
    }

}
