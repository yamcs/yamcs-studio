package org.yamcs.studio.css.core.pvmanager;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.DataSource;
import org.diirt.datasource.vtype.DataTypeSupport;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class OpsDataSource extends DataSource {

    static {
        // Without below statement PVFactories don't work
        DataTypeSupport.install();
    }

    public OpsDataSource() {
        super(true /* writeable */);
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        NamedObjectId id = NamedObjectId.newBuilder()
                .setNamespace("MDB:OPS Name")
                .setName(channelName)
                .build();
        return new ParameterChannelHandler(id);
    }
}
