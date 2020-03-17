package org.yamcs.studio.core.model;

import java.util.concurrent.CompletableFuture;

import org.yamcs.client.BulkRestDataReceiver;
import org.yamcs.protobuf.CreateTagRequest;
import org.yamcs.protobuf.EditTagRequest;
import org.yamcs.protobuf.StreamCommandsRequest;
import org.yamcs.protobuf.StreamIndexRequest;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.URLBuilder;
import org.yamcs.studio.core.client.YamcsStudioClient;
import org.yamcs.utils.TimeEncoding;

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
        String uri = "/stream-archive/" + instance + ":streamCommands";
        StreamCommandsRequest.Builder optionsb = StreamCommandsRequest.newBuilder();

        if (interval.hasStart()) {
            long start = TimeEncoding.parse(interval.getStartUTC());
            optionsb.setStart(TimeEncoding.toProtobufTimestamp(start));
        }
        if (interval.hasStop()) {
            long stop = TimeEncoding.parse(interval.getStopUTC());
            optionsb.setStop(TimeEncoding.toProtobufTimestamp(stop));
        }

        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.streamPost(uri, optionsb.build(), receiver);
    }

    public CompletableFuture<Void> downloadIndexes(String instance, TimeInterval interval,
            BulkRestDataReceiver receiver) {
        String uri = "/archive/" + instance + ":streamIndex";
        StreamIndexRequest.Builder optionsb = StreamIndexRequest.newBuilder();
        optionsb.addFilters("tm");
        optionsb.addFilters("pp");
        optionsb.addFilters("commands");
        optionsb.addFilters("completeness");
        if (interval.hasStart()) {
            long start = TimeEncoding.parse(interval.getStartUTC());
            optionsb.setStart(TimeEncoding.toProtobufTimestamp(start));
        }
        if (interval.hasStop()) {
            long stop = TimeEncoding.parse(interval.getStopUTC());
            optionsb.setStop(TimeEncoding.toProtobufTimestamp(stop));
        }

        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.streamPost(uri, optionsb.build(), receiver);
    }

    public CompletableFuture<byte[]> createTag(CreateTagRequest request) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return yamcsClient.post("/archive/" + instance + "/tags", request);
    }

    public CompletableFuture<byte[]> editTag(long tagTime, int tagId, EditTagRequest request) {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        return yamcsClient.patch("/archive/" + instance + "/tags/" + tagTime + "/" + tagId, request);
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
