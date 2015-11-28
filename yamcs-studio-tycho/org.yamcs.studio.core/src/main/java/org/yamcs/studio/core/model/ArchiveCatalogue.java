package org.yamcs.studio.core.model;

import org.yamcs.protobuf.Archive.DumpArchiveRequest;
import org.yamcs.protobuf.Archive.DumpArchiveResponse;
import org.yamcs.protobuf.Archive.GetTagsRequest;
import org.yamcs.protobuf.Archive.GetTagsResponse;
import org.yamcs.protobuf.Archive.InsertTagRequest;
import org.yamcs.protobuf.Archive.InsertTagResponse;
import org.yamcs.protobuf.Archive.UpdateTagRequest;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.NotConnectedException;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

/**
 * Groups generic archive operations (index, tags). There's is still some hornetq api that needs to
 * be refactored into this.
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

    @Deprecated
    public void dumpArchive(DumpArchiveRequest request, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        RestClient restClient = connectionManager.getRestClient();
        if (restClient != null) {
            String instance = connectionManager.getYamcsInstance();
            restClient.get("/archive" + instance, request, DumpArchiveResponse.newBuilder(), responseHandler);
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
