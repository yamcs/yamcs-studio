package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.Mdb.ContainerInfo;
import org.yamcs.protobuf.Rest.ListContainerInfoResponse;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsStudioClient;

import com.google.protobuf.InvalidProtocolBufferException;

public class ContainerCatalogue implements Catalogue {

    private static final Logger log = Logger.getLogger(ContainerCatalogue.class.getName());

    private AtomicInteger ctnClientId = new AtomicInteger(1);
    private List<ContainerInfo> containers = Collections.emptyList();
    // Indexes
    private Map<String, ContainerInfo> ContainersByQualifiedName = new LinkedHashMap<>();

    public static ContainerCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(ContainerCatalogue.class);
    }

    public int getNextContainerClientId() {
        return ctnClientId.incrementAndGet();
    }

    @Override
    public void onYamcsConnected() {
        initialiseState();
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        clearState();
        initialiseState();
    }

    @Override
    public void onYamcsDisconnected() {
        clearState();
    }

    private void initialiseState() {
        loadContainers();
    }

    private void clearState() {
        containers = Collections.emptyList();
    }

    public List<ContainerInfo> getMetaContainers() {
        return containers;
    }

    public ContainerInfo getContainerInfo(String qualifiedName) {
        return ContainersByQualifiedName.get(qualifiedName);
    }

    public synchronized void processContainers(List<ContainerInfo> containers) {
        this.containers = new ArrayList<>(containers);
        this.containers.sort((p1, p2) -> {
            return p1.getQualifiedName().compareTo(p2.getQualifiedName());
        });

        for (ContainerInfo ctn : this.containers) {
            ContainersByQualifiedName.put(ctn.getQualifiedName(), ctn);
        }
    }

    private void loadContainers() {
        log.fine("Fetching available Containers");
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        yamcsClient.get("/mdb/" + instance + "/containers", null).whenComplete((data, exc) -> {
            try {
                ListContainerInfoResponse response = ListContainerInfoResponse.parseFrom(data);
                processContainers(response.getContainerList());
            } catch (InvalidProtocolBufferException e) {
                log.log(Level.SEVERE, "Failed to decode server response", e);
            }
        });
    }

}
