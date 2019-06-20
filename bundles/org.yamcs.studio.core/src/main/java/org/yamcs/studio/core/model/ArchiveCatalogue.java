package org.yamcs.studio.core.model;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.yamcs.api.rest.BulkRestDataReceiver;
import org.yamcs.protobuf.Rest.CreateTagRequest;
import org.yamcs.protobuf.Rest.EditTagRequest;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.URLBuilder;
import org.yamcs.studio.core.client.YamcsStudioClient;

/**
 * Groups generic archive operations (index, tags).
 */
public class ArchiveCatalogue implements Catalogue {

    public static ArchiveCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(ArchiveCatalogue.class);
    }

    @Override
    public void onYamcsConnected() {
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
    }

    @Override
    public void onYamcsDisconnected() {
    }

    public CompletableFuture<Void> downloadCommands(String instance, TimeInterval interval,
            BulkRestDataReceiver receiver) {
        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/downloads/commands");
        if (interval.hasStart()) {
            urlb.setParam("start", interval.getStartUTC());
        }
        if (interval.hasStop()) {
            urlb.setParam("stop", interval.getStopUTC());
        }

        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.streamGet(urlb.toString(), null, receiver);
    }

    public CompletableFuture<Void> downloadIndexes(String instance, TimeInterval interval,
            BulkRestDataReceiver receiver) {
        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/indexes");
        urlb.setParam("filter", Arrays.asList("tm", "pp", "commands", "completeness"));
        if (interval.hasStart()) {
            urlb.setParam("start", interval.getStartUTC());
        }
        if (interval.hasStop()) {
            urlb.setParam("stop", interval.getStopUTC());
        }

        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.streamGet(urlb.toString(), null, receiver);
    }

    public CompletableFuture<byte[]> createTag(CreateTagRequest request) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return yamcsClient.post("/archive/" + instance + "/tags", request);
    }

    public CompletableFuture<byte[]> editTag(long tagTime, int tagId, EditTagRequest request) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return yamcsClient.put("/archive/" + instance + "/tags/" + tagTime + "/" + tagId, request);
    }

    public CompletableFuture<byte[]> deleteTag(long tagTime, int tagId) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return yamcsClient.delete("/archive/" + instance + "/tags/" + tagTime + "/" + tagId, null);
    }

    public CompletableFuture<byte[]> listTags(String instance, TimeInterval interval) {
        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/tags");
        if (interval.hasStart()) {
            urlb.setParam("start", interval.getStartUTC());
        }
        if (interval.hasStop()) {
            urlb.setParam("stop", interval.getStopUTC());
        }

        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.get(urlb.toString(), null);
    }
}
