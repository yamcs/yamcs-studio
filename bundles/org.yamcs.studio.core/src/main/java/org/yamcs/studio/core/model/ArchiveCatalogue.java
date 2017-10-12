package org.yamcs.studio.core.model;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.yamcs.api.rest.BulkRestDataReceiver;
import org.yamcs.protobuf.Rest.CreateTagRequest;
import org.yamcs.protobuf.Rest.EditTagRequest;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.URLBuilder;
import org.yamcs.studio.core.web.YamcsClient;

/**
 * Groups generic archive operations (index, tags).
 */
public class ArchiveCatalogue implements Catalogue {

    public static ArchiveCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(ArchiveCatalogue.class);
    }

    @Override
    public void onStudioConnect() {
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
    }

    @Override
    public void onStudioDisconnect() {
    }

    public CompletableFuture<Void> downloadCommands(TimeInterval interval, BulkRestDataReceiver receiver) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/downloads/commands");
        if (interval.hasStart())
            urlb.setParam("start", interval.getStartUTC());
        if (interval.hasStop())
            urlb.setParam("stop", interval.getStopUTC());

        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        return restClient.streamGet(urlb.toString(), null, receiver);
    }

    public CompletableFuture<Void> downloadIndexes(TimeInterval interval, BulkRestDataReceiver receiver) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/indexes");
        urlb.setParam("filter", Arrays.asList("tm", "pp", "commands", "completeness"));
        if (interval.hasStart())
            urlb.setParam("start", interval.getStartUTC());
        if (interval.hasStop())
            urlb.setParam("stop", interval.getStopUTC());

        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        return restClient.streamGet(urlb.toString(), null, receiver);
    }

    public CompletableFuture<byte[]> createTag(CreateTagRequest request) {
        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return restClient.post("/archive/" + instance + "/tags", request);
    }

    public CompletableFuture<byte[]> editTag(long tagTime, int tagId, EditTagRequest request) {
        YamcsClient yamcsClient = ConnectionManager.requireYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return yamcsClient.put("/archive/" + instance + "/tags/" + tagTime + "/" + tagId, request);
    }

    public CompletableFuture<byte[]> deleteTag(long tagTime, int tagId) {
        YamcsClient yamcsClient = ConnectionManager.requireYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return yamcsClient.delete("/archive/" + instance + "/tags/" + tagTime + "/" + tagId, null);
    }

    public CompletableFuture<byte[]> listTags(TimeInterval interval) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();

        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/tags");
        if (interval.hasStart())
            urlb.setParam("start", interval.getStartUTC());
        if (interval.hasStop())
            urlb.setParam("stop", interval.getStopUTC());

        YamcsClient yamcsClient = ConnectionManager.requireYamcsClient();
        return yamcsClient.get(urlb.toString(), null);
    }
}
