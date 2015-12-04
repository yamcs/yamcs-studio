package org.yamcs.studio.core.model;

import java.util.Arrays;

import org.yamcs.protobuf.Archive.InsertTagRequest;
import org.yamcs.protobuf.Archive.InsertTagResponse;
import org.yamcs.protobuf.Archive.ListTagsResponse;
import org.yamcs.protobuf.Archive.PatchTagRequest;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.NotConnectedException;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.core.web.URLBuilder;

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
    public void onStudioDisconnect() {
    }

    public void downloadCommands(TimeInterval interval, ResponseHandler responseHandler) {
        String instance = ConnectionManager.getInstance().getYamcsInstance();
        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/commands");
        if (interval.hasStart())
            urlb.setParam("start", interval.getStartUTC());
        if (interval.hasStop())
            urlb.setParam("stop", interval.getStopUTC());

        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient != null) {
            restClient.streamGet(urlb.toString(), null, () -> CommandHistoryEntry.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void downloadIndexes(TimeInterval interval, ResponseHandler responseHandler) {
        String instance = ConnectionManager.getInstance().getYamcsInstance();
        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/indexes?filter=tm,pp,commands,completeness");
        urlb.setParam("filter", Arrays.asList("tm", "pp", "commands", "completeness"));
        if (interval.hasStart())
            urlb.setParam("start", interval.getStartUTC());
        if (interval.hasStop())
            urlb.setParam("stop", interval.getStopUTC());

        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient != null) {
            restClient.streamGet(urlb.toString(), null, () -> IndexResult.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void insertTag(InsertTagRequest request, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            String instance = connectionManager.getYamcsInstance();
            restClient.post("/archive/" + instance + "/tags", request, InsertTagResponse.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void updateTag(long tagTime, int tagId, PatchTagRequest request, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            String instance = connectionManager.getYamcsInstance();
            restClient.put("/archive/" + instance + "/tags/" + tagTime + "/" + tagId, request, null, responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void deleteTag(long tagTime, int tagId, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            String instance = connectionManager.getYamcsInstance();
            restClient.delete("/archive/" + instance + "/tags/" + tagTime + "/" + tagId, null, null, responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void listTags(TimeInterval interval, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        String instance = connectionManager.getYamcsInstance();

        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/tags");
        if (interval.hasStart())
            urlb.setParam("start", interval.getStartUTC());
        if (interval.hasStop())
            urlb.setParam("stop", interval.getStopUTC());

        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            restClient.get(urlb.toString(), null, ListTagsResponse.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }
}
