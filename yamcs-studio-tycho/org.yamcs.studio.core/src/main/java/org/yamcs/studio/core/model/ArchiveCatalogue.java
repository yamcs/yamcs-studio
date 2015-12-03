package org.yamcs.studio.core.model;

import org.yamcs.protobuf.Archive.GetTagsRequest;
import org.yamcs.protobuf.Archive.GetTagsResponse;
import org.yamcs.protobuf.Archive.InsertTagRequest;
import org.yamcs.protobuf.Archive.InsertTagResponse;
import org.yamcs.protobuf.Archive.UpdateTagRequest;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.NotConnectedException;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.utils.TimeEncoding;

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

    public void downloadCommands(long start, long stop, ResponseHandler responseHandler) {
        String instance = ConnectionManager.getInstance().getYamcsInstance();
        String resource = "/archive/" + instance + "/commands";
        if (start != TimeEncoding.INVALID_INSTANT) {
            resource += "?start=" + start;
            if (stop != TimeEncoding.INVALID_INSTANT) {
                resource += "&stop=" + stop;
            }
        } else if (stop != TimeEncoding.INVALID_INSTANT) {
            resource += "?stop=" + stop;
        }
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient != null) {
            restClient.streamGet(resource, null, () -> CommandHistoryEntry.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void downloadIndexes(long start, long stop, ResponseHandler responseHandler) {
        String instance = ConnectionManager.getInstance().getYamcsInstance();
        String resource = "/archive/" + instance + "/indexes?filter=tm,pp,commands,completeness";
        if (start != TimeEncoding.INVALID_INSTANT) {
            resource += "&start=" + start;
        }
        if (stop != TimeEncoding.INVALID_INSTANT) {
            resource += "&stop=" + stop;
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

    public void updateTag(long tagTime, int tagId, UpdateTagRequest request, ResponseHandler responseHandler) {
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

    public void getTags(GetTagsRequest request, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            String instance = connectionManager.getYamcsInstance();
            restClient.get("/archive/" + instance + "/tags", request, GetTagsResponse.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }
}
