package org.yamcs.studio.ui.archive;

import org.hornetq.api.core.HornetQException;
import org.yamcs.YamcsException;
import org.yamcs.api.ConnectionListener;
import org.yamcs.api.Protocol;
import org.yamcs.api.YamcsClient;
import org.yamcs.api.YamcsConnector;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.DeleteTagRequest;
import org.yamcs.protobuf.Yamcs.IndexRequest;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.protobuf.Yamcs.TagResult;
import org.yamcs.protobuf.Yamcs.UpsertTagRequest;

public class ArchiveIndexReceiver implements ConnectionListener {
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

    public void getIndex(final String instance, final TimeInterval interval) {
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
                        IndexResult ir = (IndexResult) yamcsClient.receiveData(IndexResult.newBuilder());
                        //    System.out.println("Received ")
                        if (ir == null) {
                            archiveView.receiveArchiveRecordsFinished();
                            break;
                        }
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

    public void getTag(final String instance, final TimeInterval interval) {
        //System.out.println("receiving tags for "+instance);
        if (receiving) {
            archiveView.log("already receiving data");
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
                    yamcsClient.sendRequest(Protocol.getYarchIndexControlAddress(instance), "getTag", request.build());
                    while (true) {
                        TagResult tr = (TagResult) yamcsClient.receiveData(TagResult.newBuilder());
                        if (tr == null) {
                            archiveView.receiveTagsFinished();
                            break;
                        }
                        archiveView.receiveTags(tr.getTagList());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    archiveView.receiveArchiveRecordsError(e.getMessage());
                } finally {
                    receiving = false;
                }
            };
        };
        receivingThread.start();
    }

    public void insertTag(String instance, ArchiveTag tag) {
        UpsertTagRequest utr = UpsertTagRequest.newBuilder().setNewTag(tag).build();
        try {
            ArchiveTag ntag = (ArchiveTag) yamcsClient.executeRpc((Protocol.getYarchIndexControlAddress(instance)), "upsertTag", utr,
                    ArchiveTag.newBuilder());
            archiveView.tagAdded(ntag);
        } catch (Exception e) {
            archiveView.log("Failed to insert tag: " + e.getMessage());
        }
    }

    public void updateTag(String instance, ArchiveTag oldTag, ArchiveTag newTag) {
        UpsertTagRequest utr = UpsertTagRequest.newBuilder().setOldTag(oldTag).setNewTag(newTag).build();
        try {
            ArchiveTag ntag = (ArchiveTag) yamcsClient.executeRpc((Protocol.getYarchIndexControlAddress(instance)), "upsertTag", utr,
                    ArchiveTag.newBuilder());
            archiveView.tagChanged(oldTag, ntag);
        } catch (Exception e) {
            archiveView.log("Failed to insert tag: " + e.getMessage());
        }
    }

    public void deleteTag(String instance, ArchiveTag tag) {
        DeleteTagRequest dtr = DeleteTagRequest.newBuilder().setTag(tag).build();
        try {
            ArchiveTag rtag = (ArchiveTag) yamcsClient.executeRpc((Protocol.getYarchIndexControlAddress(instance)), "deleteTag", dtr,
                    ArchiveTag.newBuilder());
            archiveView.tagRemoved(rtag);
        } catch (Exception e) {
            archiveView.log("Failed to remove tag: " + e.getMessage());
        }
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
