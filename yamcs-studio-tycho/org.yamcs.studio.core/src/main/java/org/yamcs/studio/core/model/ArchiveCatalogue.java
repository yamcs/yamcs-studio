package org.yamcs.studio.core.model;

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
        String resource = "/archive/" + instance + "/commands";
        if (interval.hasStart()) {
            resource += "?start=" + interval.getStart();
            if (interval.hasStop()) {
                resource += "&stop=" + interval.getStop();
            }
        } else if (interval.hasStop()) {
            resource += "?stop=" + interval.getStop();
        }
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient != null) {
            restClient.streamGet(resource, null, () -> CommandHistoryEntry.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void downloadIndexes(TimeInterval interval, ResponseHandler responseHandler) {
        String instance = ConnectionManager.getInstance().getYamcsInstance();
        String resource = "/archive/" + instance + "/indexes?filter=tm,pp,commands,completeness";
        if (interval.hasStart()) {
            resource += "&start=" + interval.getStart();
        }
        if (interval.hasStop()) {
            resource += "&stop=" + interval.getStop();
        }
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient != null) {
            restClient.streamGet(resource, null, () -> IndexResult.newBuilder(), responseHandler);
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
        String resource = "/archive/" + instance + "/tags";
        if (interval.hasStart()) {
            resource += "?start=" + interval.getStart();
            if (interval.hasStop()) {
                resource += "&stop=" + interval.getStop();
            }
        } else if (interval.hasStop()) {
            resource += "?stop=" + interval.getStop();
        }

        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            restClient.get(resource, null, ListTagsResponse.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }
}
