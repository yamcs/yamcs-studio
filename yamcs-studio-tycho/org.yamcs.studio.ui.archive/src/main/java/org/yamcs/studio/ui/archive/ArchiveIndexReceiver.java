package org.yamcs.studio.ui.archive;

import java.util.logging.Logger;

import org.hornetq.api.core.HornetQException;
import org.yamcs.YamcsException;
import org.yamcs.api.ConnectionListener;
import org.yamcs.api.Protocol;
import org.yamcs.api.YamcsClient;
import org.yamcs.api.YamcsConnector;
import org.yamcs.protobuf.Archive.GetTagsRequest;
import org.yamcs.protobuf.Archive.GetTagsResponse;
import org.yamcs.protobuf.Archive.InsertTagRequest;
import org.yamcs.protobuf.Archive.InsertTagResponse;
import org.yamcs.protobuf.Archive.UpdateTagRequest;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexRequest;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.studio.core.model.ArchiveCatalogue;
import org.yamcs.studio.core.ui.utils.TimeInterval;
import org.yamcs.studio.core.web.ResponseHandler;

import com.google.protobuf.MessageLite;

public class ArchiveIndexReceiver implements ConnectionListener {

    private static final Logger log = Logger.getLogger(ArchiveIndexReceiver.class.getName());
    private ArchiveView archiveView;

    volatile private boolean receiving = false;

    private YamcsConnector yconnector;
    private YamcsClient yamcsClient;

    public ArchiveIndexReceiver(YamcsConnector yconnector) {
        this.yconnector = yconnector;
        yconnector.addConnectionListener(this);
    }

    public void setIndexListener(ArchiveView archiveView) {
        this.archiveView = archiveView;
    }

    public void getIndex(String instance, TimeInterval interval) {
        if (receiving) {
            archiveView.log("already receiving data");
            return;
        }
        if (instance == null) {
            archiveView.receiveArchiveRecordsError("No yamcs instance to get data from");
            return;
        }
        Thread receivingThread = new Thread() {
            @Override
            public void run() {
                try {
                    IndexRequest.Builder request = IndexRequest.newBuilder().setInstance(instance);
                    if (interval.hasStart())
                        request.setStart(interval.getStart());
                    if (interval.hasStop())
                        request.setStop(interval.getStop());
                    request.setSendAllPp(true).setSendAllTm(true).setSendAllCmd(true);
                    request.setSendCompletenessIndex(true);
                    //       yamcsClient.executeRpc(Protocol.getYarchIndexControlAddress(instance), "getIndex", request.build(), null);
                    yamcsClient.sendRequest(Protocol.getYarchIndexControlAddress(instance), "getIndex", request.build());
                    while (true) {
                        log.info("Ready to receive another archive chunk");
                        IndexResult ir = (IndexResult) yamcsClient.receiveData(IndexResult.newBuilder());
                        if (ir == null) {
                            log.info("Done receiving archive records.");
                            archiveView.receiveArchiveRecordsFinished();
                            break;
                        }
                        log.info(String.format("Received %d archive records", ir.getRecordsCount()));
                        archiveView.receiveArchiveRecords(ir);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    archiveView.receiveArchiveRecordsError(e.toString());
                } finally {
                    receiving = false;
                }
            };
        };
        receivingThread.start();
    }

    public void getTag(TimeInterval interval) {
        if (receiving) {
            archiveView.log("already receiving data");
            return;
        }
        GetTagsRequest.Builder requestb = GetTagsRequest.newBuilder();
        if (interval.hasStart())
            requestb.setStart(interval.getStart());
        if (interval.hasStop())
            requestb.setStop(interval.getStop());
        ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
        catalogue.getTags(requestb.build(), new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                GetTagsResponse response = (GetTagsResponse) responseMsg;
                archiveView.receiveTags(response.getTagsList());
                archiveView.receiveTagsFinished();
                receiving = false;
            }

            @Override
            public void onException(Exception e) {
                archiveView.log("Failed to retreive tags: " + e.getMessage());
                receiving = false;
            }
        });
    }

    public void insertTag(ArchiveTag tag) {
        InsertTagRequest.Builder requestb = InsertTagRequest.newBuilder();
        if (tag.hasName())
            requestb.setName(tag.getName());
        if (tag.hasColor())
            requestb.setColor(tag.getColor());
        if (tag.hasDescription())
            requestb.setDescription(tag.getDescription());
        if (tag.hasStart())
            requestb.setStart(tag.getStart());
        if (tag.hasStop())
            requestb.setStop(tag.getStop());
        ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
        catalogue.insertTag(requestb.build(), new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                InsertTagResponse response = (InsertTagResponse) responseMsg;
                archiveView.tagAdded(response.getTag());
            }

            @Override
            public void onException(Exception e) {
                archiveView.log("Failed to insert tag: " + e.getMessage());
            }
        });
    }

    public void updateTag(ArchiveTag oldTag, ArchiveTag newTag) {
        UpdateTagRequest.Builder requestb = UpdateTagRequest.newBuilder();
        if (newTag.hasName())
            requestb.setName(newTag.getName());
        if (newTag.hasColor())
            requestb.setColor(newTag.getColor());
        if (newTag.hasDescription())
            requestb.setDescription(newTag.getDescription());
        if (newTag.hasStart())
            requestb.setStart(newTag.getStart());
        if (newTag.hasStop())
            requestb.setStop(newTag.getStop());
        long tagTime = oldTag.hasStart() ? oldTag.getStart() : 0;
        int tagId = oldTag.getId();
        ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
        catalogue.updateTag(tagTime, tagId, requestb.build(), new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                archiveView.tagChanged(oldTag, newTag);
            }

            @Override
            public void onException(Exception e) {
                archiveView.log("Failed to insert tag: " + e.getMessage());
            }
        });
    }

    public void deleteTag(ArchiveTag tag) {
        long tagTime = tag.hasStart() ? tag.getStart() : 0;
        int tagId = tag.getId();
        ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
        catalogue.updateTag(tagTime, tagId, null, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                archiveView.tagRemoved(tag);
            }

            @Override
            public void onException(Exception e) {
                archiveView.log("Failed to remove tag: " + e.getMessage());
            }
        });
    }

    public boolean supportsTags() {
        return true;
    }

    @Override
    public void connecting(String url) {
        archiveView.connecting(url);
    }

    @Override
    public void connected(String url) {
        try {
            yamcsClient = yconnector.getSession().newClientBuilder().setRpc(true).setDataConsumer(null, null).build();
            archiveView.log("connected to " + yconnector.getUrl());
        } catch (HornetQException e) {
            e.printStackTrace();
            archiveView.log("Failed to build yamcs client: " + e.getMessage());
        }
    }

    @Override
    public void connectionFailed(String url, YamcsException exception) {
        archiveView.connectionFailed(url, exception);
    }

    @Override
    public void disconnected() {
        archiveView.disconnected();
    }

    @Override
    public void log(String message) {
        archiveView.log(message);
    }
}
