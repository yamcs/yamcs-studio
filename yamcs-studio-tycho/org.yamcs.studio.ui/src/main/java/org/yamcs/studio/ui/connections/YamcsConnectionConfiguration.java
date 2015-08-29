package org.yamcs.studio.ui.connections;

import java.util.ArrayList;
import java.util.List;

import org.yamcs.api.ws.YamcsConnectionProperties;

public class YamcsConnectionConfiguration {

    private List<YamcsConnectionProperties> connectionPropertiesList = new ArrayList<>();
    private YamcsConnectionProperties lastConnectionProperties;
    private boolean autoConnect = false;

    public YamcsConnectionConfiguration() {
        connectionPropertiesList.add(new YamcsConnectionProperties("localhost", 8090, "simulator"));
    }

    public List<YamcsConnectionProperties> getConnectionPropertiesList() {
        return connectionPropertiesList;
    }

    public YamcsConnectionProperties getLastConnectionProperties() {
        return lastConnectionProperties;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }
}
