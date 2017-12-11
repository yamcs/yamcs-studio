package org.yamcs.studio.ui.clients;

import java.util.ArrayList;
import java.util.List;

import org.yamcs.protobuf.YamcsManagement.ClientInfo;

public class ClientsTableModel {

    private List<ClientInfo> clients = new ArrayList<>();
    private ClientsTableViewer clientsTableViewer;

    public ClientsTableModel(ClientsTableViewer clientsTableViewer) {
        this.clientsTableViewer = clientsTableViewer;
    }

    public void updateClient(ClientInfo uci) {
        boolean found = false;
        for (int i = 0; i < clients.size(); ++i) {
            ClientInfo ci = clients.get(i);
            if (ci.getId() == uci.getId()) {
                clients.set(i, uci);
                clientsTableViewer.replace(uci, i);
                found = true;
                break;
            }
        }
        if (!found) {
            clients.add(uci);
            clientsTableViewer.add(uci);
        }

    }

    public void removeClient(ClientInfo rci) {
        for (ClientInfo ci : clients) {
            if (ci.getId() == rci.getId()) {
                clients.remove(ci);
                clientsTableViewer.remove(ci);
                break;
            }
        }
    }
}
