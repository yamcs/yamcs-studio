package org.yamcs.studio.ui.clients;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.yamcs.protobuf.YamcsManagement.ClientInfo;

public class ClientsTableModel {
    private static final Logger log = Logger.getLogger(ClientsTableModel.class.getName());
    private ArrayList<ClientInfo> clients = new ArrayList<ClientInfo>();
    private ClientsTableViewer clientsTableViewer;

    public ClientsTableModel(ClientsTableViewer clientsTableViewer)
    {
        this.clientsTableViewer = clientsTableViewer;
    }

    public void updateClient(ClientInfo uci) {
        try {
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
        } catch (Exception e)
        {
            log.severe(e.toString());
        }

    }

    public void removeClient(ClientInfo rci) {
        try {
            boolean found = false;
            for (int i = 0; i < clients.size(); ++i) {
                ClientInfo ci = clients.get(i);
                if (ci.getId() == rci.getId()) {
                    clients.remove(ci);
                    clientsTableViewer.remove(ci);
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.warning("client not found in list: " + rci);
            }
        } catch (Exception e)
        {
            log.severe(e.toString());
        }

    }
}
