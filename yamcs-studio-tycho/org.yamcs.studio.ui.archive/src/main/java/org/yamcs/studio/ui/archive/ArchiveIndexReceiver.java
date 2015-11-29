package org.yamcs.studio.ui.archive;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protobuf.Archive.GetTagsRequest;
import org.yamcs.protobuf.Archive.GetTagsResponse;
import org.yamcs.protobuf.Archive.InsertTagRequest;
import org.yamcs.protobuf.Archive.InsertTagResponse;
import org.yamcs.protobuf.Archive.UpdateTagRequest;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.studio.core.model.ArchiveCatalogue;
import org.yamcs.studio.core.ui.utils.TimeInterval;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.MessageLite;

public class ArchiveIndexReceiver {

    private static final Logger log = Logger.getLogger(ArchiveIndexReceiver.class.getName());
    private ArchiveView archiveView;

    volatile private boolean receiving = false;

    public void setIndexListener(ArchiveView archiveView) {
        this.archiveView = archiveView;
    }

    public void getIndex(TimeInterval interval) {
        if (receiving) {
            log.info("already receiving data");
            return;
        }

        ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
        long start = interval.hasStart() ? interval.getStart() : TimeEncoding.INVALID_INSTANT;
        long stop = interval.hasStop() ? interval.getStop() : TimeEncoding.INVALID_INSTANT;
        catalogue.downloadIndexes(start, stop, new ResponseHandler() {

            @Override
            public void onMessage(MessageLite responseMsg) {
                if (responseMsg != null) {
                    IndexResult response = (IndexResult) responseMsg;
                    log.info(String.format("Received %d archive records", response.getRecordsCount()));
                    archiveView.receiveArchiveRecords(response);
                } else {
                    log.info("Done receiving archive records.");
                    archiveView.receiveArchiveRecordsFinished();
                    receiving = false;
                }
            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
                archiveView.receiveArchiveRecordsError(e.toString());
            }
        });
    }

    public void getTag(TimeInterval interval) {
        if (receiving) {
            log.info("Already receiving data");
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
                log.log(Level.SEVERE, "Failed to retreive tags", e);
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
                log.log(Level.SEVERE, "Failed to insert tag", e);
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
                log.log(Level.SEVERE, "Failed to insert tag", e);
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
                log.log(Level.SEVERE, "Failed to remove tag", e);
            }
        });
    }
}
