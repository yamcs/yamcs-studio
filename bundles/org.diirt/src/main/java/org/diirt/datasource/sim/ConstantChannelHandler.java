/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.sim;

import java.util.List;
import java.util.logging.Logger;
import org.diirt.datasource.ChannelWriteCallback;
import org.diirt.datasource.MultiplexedChannelHandler;
import org.diirt.datasource.util.FunctionParser;
import org.diirt.vtype.ValueFactory;

/**
 *
 * @author carcassi
 */
class ConstantChannelHandler extends MultiplexedChannelHandler<Object, Object> {

    private static final Logger log = Logger.getLogger(ConstantChannelHandler.class.getName());

    public ConstantChannelHandler(String channelName) {
        super(channelName);
        List<Object> tokens = FunctionParser.parseFunctionWithScalarOrArrayArguments(channelName,
                "Wrong syntax. Correct examples: const(3.14), const(\"Bob\"), const(1,2,3), const(\"ON\", \"OFF\"");
        processMessage((Object) ValueFactory.toVTypeChecked(tokens.get(1)));
    }

    @Override
    public void connect() {
        processConnection(new Object());
    }

    @Override
    public void disconnect() {
        processConnection(null);
    }

    @Override
    protected boolean saveMessageAfterDisconnect() {
        return true;
    }

    @Override
    public void write(Object newValue, ChannelWriteCallback callback) {
        throw new UnsupportedOperationException("Can't write to simulation channel.");
    }
}
