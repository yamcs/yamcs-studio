package org.yamcs.studio.core.model;

import java.util.Arrays;

import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Rest.CreateTagRequest;
import org.yamcs.protobuf.Rest.EditTagRequest;
import org.yamcs.protobuf.Rest.ListTagsResponse;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.NotConnectedException;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.YamcsClient;
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
    public void instanceChanged(String oldInstance, String newInstance) {
    }

    @Override
    public void onStudioDisconnect() {
    }

    public void downloadCommands(TimeInterval interval, ResponseHandler responseHandler) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/downloads/commands");
        if (interval.hasStart())
            urlb.setParam("start", interval.getStartUTC());
        if (interval.hasStop())
            urlb.setParam("stop", interval.getStopUTC());

        YamcsClient restClient = ConnectionManager.getInstance().getYamcsClient();
        if (restClient != null) {
            restClient.streamGet(urlb.toString(), null, () -> CommandHistoryEntry.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void downloadIndexes(TimeInterval interval, ResponseHandler responseHandler) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/indexes");
        urlb.setParam("filter", Arrays.asList("tm", "pp", "commands", "completeness"));
        if (interval.hasStart())
            urlb.setParam("start", interval.getStartUTC());
        if (interval.hasStop())
            urlb.setParam("stop", interval.getStopUTC());

        YamcsClient restClient = ConnectionManager.getInstance().getYamcsClient();
        if (restClient != null) {
            restClient.streamGet(urlb.toString(), null, () -> IndexResult.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void createTag(CreateTagRequest request, ResponseHandler responseHandler) {
        YamcsClient restClient = ConnectionManager.getInstance().getYamcsClient();
        if (restClient != null) {
            String instance = ManagementCatalogue.getCurrentYamcsInstance();
            restClient.post("/archive/" + instance + "/tags", request, ArchiveTag.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void editTag(long tagTime, int tagId, EditTagRequest request, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        YamcsClient restClient = connectionManager.getYamcsClient();
        if (restClient != null) {
            String instance = ManagementCatalogue.getCurrentYamcsInstance();
            restClient.put("/archive/" + instance + "/tags/" + tagTime + "/" + tagId, request, null, responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void deleteTag(long tagTime, int tagId, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        YamcsClient restClient = connectionManager.getYamcsClient();
        if (restClient != null) {
            String instance = ManagementCatalogue.getCurrentYamcsInstance();
            restClient.delete("/archive/" + instance + "/tags/" + tagTime + "/" + tagId, null, null, responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void listTags(TimeInterval interval, ResponseHandler responseHandler) {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        String instance = ManagementCatalogue.getCurrentYamcsInstance();

        URLBuilder urlb = new URLBuilder("/archive/" + instance + "/tags");
        if (interval.hasStart())
            urlb.setParam("start", interval.getStartUTC());
        if (interval.hasStop())
            urlb.setParam("stop", interval.getStopUTC());

        YamcsClient restClient = connectionManager.getYamcsClient();
        if (restClient != null) {
            restClient.get(urlb.toString(), null, ListTagsResponse.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }
}
