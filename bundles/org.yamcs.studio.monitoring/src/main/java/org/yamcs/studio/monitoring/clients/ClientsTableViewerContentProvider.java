package org.yamcs.studio.monitoring.clients;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.yamcs.protobuf.ClientInfo;

/**
 * Manages data for all links (regardless of which instance). Based on external configuration, it will switch the
 * content of the associated table viewer to only show links for a specific Yamcs instance.
 */
public class ClientsTableViewerContentProvider implements IStructuredContentProvider {

    private Map<Integer, ClientInfo> clientsById = new HashMap<>();

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return clientsById.values().toArray();
    }

    public void processClientUpdate(ClientInfo incoming) {
        clientsById.put(incoming.getId(), incoming);
    }

    public void processClientDisconnect(ClientInfo incoming) {
        clientsById.remove(incoming.getId());
    }

    public void clearAll() {
        clientsById.clear();
    }
}
