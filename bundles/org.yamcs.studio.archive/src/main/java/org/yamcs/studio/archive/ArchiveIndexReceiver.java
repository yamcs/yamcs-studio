package org.yamcs.studio.archive;

import java.time.Instant;
import java.util.logging.Logger;

import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.client.archive.ArchiveClient.IndexOptions;
import org.yamcs.protobuf.CreateTagRequest;
import org.yamcs.protobuf.EditTagRequest;
import org.yamcs.protobuf.Yamcs.ArchiveTag;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;

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

        ArchiveClient archive = YamcsPlugin.getArchiveClient();
        if (archive != null) {
            archive.streamIndex(response -> {
                log.fine(String.format("Received %d archive records", response.getRecordsCount()));
                archiveView.receiveArchiveRecords(response);
            }, interval.getStart(), interval.getStop(), IndexOptions.filter("tm", "pp", "commands"/*, "completeness"*/))
                    .whenComplete((data, exc) -> {
                        if (exc == null) {
                            log.info("Done receiving archive records");
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

        ArchiveClient archive = YamcsPlugin.getArchiveClient();
        if (archive != null) {
            archive.listTags(interval.getStart(), interval.getStop()).whenComplete((tags, exc) -> {
                if (exc == null) {
                    archiveView.receiveTags(tags);
                    archiveView.receiveTagsFinished();
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
            Instant start = Instant.ofEpochSecond(tag.getStartUTC().getSeconds(), tag.getStartUTC().getNanos());
            requestb.setStart(start.toString());
        }
        if (tag.hasStop()) {
            Instant stop = Instant.ofEpochSecond(tag.getStopUTC().getSeconds(), tag.getStopUTC().getNanos());
            requestb.setStop(stop.toString());
        }
        ArchiveClient archive = YamcsPlugin.getArchiveClient();
        archive.createTag(requestb.build()).whenComplete((response, exc) -> {
            if (exc == null) {
                archiveView.tagAdded(response);
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
            Instant start = Instant.ofEpochSecond(newTag.getStartUTC().getSeconds(), newTag.getStartUTC().getNanos());
            requestb.setStart(start.toString());
        }
        if (newTag.hasStop()) {
            Instant stop = Instant.ofEpochSecond(newTag.getStopUTC().getSeconds(), newTag.getStopUTC().getNanos());
            requestb.setStop(stop.toString());
        }

        requestb.setTagTime(oldTag.hasStart() ? oldTag.getStart() : 0);
        requestb.setTagId(oldTag.getId());
        ArchiveClient archive = YamcsPlugin.getArchiveClient();
        archive.updateTag(requestb.build()).thenRun(() -> {
            archiveView.tagChanged(oldTag, newTag);
        });
    }

    public void deleteTag(ArchiveTag tag) {
        long tagTime = tag.hasStart() ? tag.getStart() : 0;
        int tagId = tag.getId();
        ArchiveClient archive = YamcsPlugin.getArchiveClient();
        archive.deleteTag(tagTime, tagId).thenRun(() -> {
            archiveView.tagRemoved(tag);
        });
    }
}
