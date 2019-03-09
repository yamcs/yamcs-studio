/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.loc;

import java.util.ArrayList;
import java.util.List;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.ChannelReadRecipe;
import org.diirt.datasource.ChannelWriteRecipe;
import org.diirt.datasource.DataSource;
import org.diirt.datasource.ReadRecipe;
import org.diirt.datasource.WriteRecipe;
import org.diirt.datasource.util.FunctionParser;
import org.diirt.datasource.vtype.DataTypeSupport;

/**
 * Data source for locally written data. Each instance of this data source will have its own separate channels and
 * values.
 *
 * @author carcassi
 */
public class LocalDataSource extends DataSource {

    static {
        // Install type support for the types it generates.
        DataTypeSupport.install();
    }

    /**
     * Creates a new data source.
     */
    public LocalDataSource() {
        super(true);
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        // Parse the channel name
        List<Object> parsedTokens = parseName(channelName);

        LocalChannelHandler channel = new LocalChannelHandler(parsedTokens.get(0).toString());
        return channel;
    }

    private List<Object> parseName(String channelName) {
        List<Object> tokens = FunctionParser.parseFunctionAnyParameter(".+", channelName);
        String nameAndType = tokens.get(0).toString();
        String name = nameAndType;
        String type = null;
        int index = nameAndType.lastIndexOf('<');
        if (nameAndType.endsWith(">") && index != -1) {
            name = nameAndType.substring(0, index);
            type = nameAndType.substring(index + 1, nameAndType.length() - 1);
        }
        List<Object> newTokens = new ArrayList<>();
        newTokens.add(name);
        newTokens.add(type);
        Object initialValue;
        if ("VEnum".equals(type)) {
            List<Object> initialValueList = new ArrayList<>();
            initialValueList.add(tokens.remove(1));
            Object labels = FunctionParser.asScalarOrList(tokens.subList(1, tokens.size()));
            if (!(labels instanceof List<?>)) {
                throw new RuntimeException("Invalid format for VEnum channel.");
            }
            initialValueList.add(labels);
            initialValue = initialValueList;
        } else {
            initialValue = FunctionParser.asScalarOrList(tokens.subList(1, tokens.size()));
        }
        if (tokens.size() > 1) {
            newTokens.add(initialValue);
        }
        return newTokens;
    }

    @Override
    protected String channelHandlerLookupName(String channelName) {
        List<Object> parsedTokens = parseName(channelName);
        return parsedTokens.get(0).toString();
    }

    private void initialize(String channelName) {
        List<Object> parsedTokens = parseName(channelName);

        LocalChannelHandler channel = (LocalChannelHandler) getChannels().get(channelHandlerLookupName(channelName));
        channel.setType((String) parsedTokens.get(1));
        if (parsedTokens.size() > 2) {
            if (channel != null) {
                channel.setInitialValue(parsedTokens.get(2));
            }
        }
    }

    @Override
    public void connectRead(ReadRecipe readRecipe) {
        super.connectRead(readRecipe);

        // Initialize all values
        for (ChannelReadRecipe channelReadRecipe : readRecipe.getChannelReadRecipes()) {
            initialize(channelReadRecipe.getChannelName());
        }
    }

    @Override
    public void connectWrite(WriteRecipe writeRecipe) {
        super.connectWrite(writeRecipe);

        // Initialize all values
        for (ChannelWriteRecipe channelWriteRecipe : writeRecipe.getChannelWriteRecipes()) {
            initialize(channelWriteRecipe.getChannelName());
        }
    }

}
