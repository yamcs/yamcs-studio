package org.yamcs.studio.archive;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.client.ClientException;
import org.yamcs.protobuf.CreateTagRequest;
import org.yamcs.protobuf.EditTagRequest;
import org.yamcs.protobuf.ListTagsResponse;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.protobuf.Yamcs.IndexResult;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.model.ArchiveCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.InvalidProtocolBufferException;

public class ArchiveIndexReceiver {

    private static final Logger log = Logger.getLogger(ArchiveIndexReceiver.class.getName());
    private ArchiveView archiveView;

    volatile private boolean receiving = false;

    public void setIndexListener(ArchiveView archiveView) {
        this.archiveView = archiveView;
    }

    public void getIndex(TimeInterval interval) {
        if (receiving) {
            log.fine("Already receiving data");
            return;
        }

        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        if (instance != null) {
            ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
            catalogue.downloadIndexes(instance, interval, data -> {
                try {
                    IndexResult response = IndexResult.parseFrom(data);
                    log.fine(String.format("Received %d archive records", response.getRecordsCount()));
                    archiveView.receiveArchiveRecords(response);
                } catch (InvalidProtocolBufferException e) {
                    throw new ClientException("Failed to decode server message", e);
                }
            }).whenComplete((data, exc) -> {
                if (exc == null) {
                    log.info("Done receiving archive records.");
                    archiveView.receiveArchiveRecordsFinished();
                    receiving = false;
                } else {
                    archiveView.receiveArchiveRecordsError(exc.toString());
                }
            });
        } else {
            archiveView.receiveArchiveRecordsFinished();
            receiving = false;
        }
    }

    public void getTag(TimeInterval interval) {
        if (receiving) {
            log.info("Already receiving data");
            return;
        }

        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        if (instance != null) {
            ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
            catalogue.listTags(instance, interval).whenComplete((data, exc) -> {
                if (exc == null) {
                    try {
                        ListTagsResponse response = ListTagsResponse.parseFrom(data);
                        archiveView.receiveTags(response.getTagList());
                        archiveView.receiveTagsFinished();
                    } catch (InvalidProtocolBufferException e) {
                        log.log(Level.SEVERE, "Failed to decode server message", e);
                    }
                }
                receiving = false;
            });
        } else {
            archiveView.receiveTagsFinished();
            receiving = false;
        }
    }

    public void createTag(ArchiveTag tag) {
        CreateTagRequest.Builder requestb = CreateTagRequest.newBuilder();
        if (tag.hasName()) {
            requestb.setName(tag.getName());
        }
        if (tag.hasColor()) {
            requestb.setColor(tag.getColor());
        }
        if (tag.hasDescription()) {
            requestb.setDescription(tag.getDescription());
        }
        if (tag.hasStart()) {
            requestb.setStart(TimeEncoding.toString(tag.getStart()));
        }
        if (tag.hasStop()) {
            requestb.setStop(TimeEncoding.toString(tag.getStop()));
        }
        ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
        catalogue.createTag(requestb.build()).whenComplete((data, exc) -> {
            if (exc != null) {
                ArchiveTag response;
                try {
                    response = ArchiveTag.parseFrom(data);
                    archiveView.tagAdded(response);
                } catch (InvalidProtocolBufferException e) {
                    log.log(Level.SEVERE, "Failed to decode server message", e);
                }
            }
        });
    }

    public void updateTag(ArchiveTag oldTag, ArchiveTag newTag) {
        EditTagRequest.Builder requestb = EditTagRequest.newBuilder();
        if (newTag.hasName()) {
            requestb.setName(newTag.getName());
        }
        if (newTag.hasColor()) {
            requestb.setColor(newTag.getColor());
        }
        if (newTag.hasDescription()) {
            requestb.setDescription(newTag.getDescription());
        }
        if (newTag.hasStart()) {
            requestb.setStart(TimeEncoding.toString(newTag.getStart()));
        }
        if (newTag.hasStop()) {
            requestb.setStop(TimeEncoding.toString(newTag.getStop()));
        }
        long tagTime = oldTag.hasStart() ? oldTag.getStart() : 0;
        int tagId = oldTag.getId();
        ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
        catalogue.editTag(tagTime, tagId, requestb.build()).thenRun(() -> {
            archiveView.tagChanged(oldTag, newTag);
        });
    }

    public void deleteTag(ArchiveTag tag) {
        long tagTime = tag.hasStart() ? tag.getStart() : 0;
        int tagId = tag.getId();
        ArchiveCatalogue catalogue = ArchiveCatalogue.getInstance();
        catalogue.deleteTag(tagTime, tagId).thenRun(() -> {
            archiveView.tagRemoved(tag);
        });
    }
}
