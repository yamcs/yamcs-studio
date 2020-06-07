package org.yamcs.studio.commanding.cmdhist;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Commanding.CommandId;
import org.yamcs.studio.commanding.PTVInfo;
import org.yamcs.studio.commanding.cmdhist.CommandHistoryRecord.CommandState;

public class CommandHistoryRecordContentProvider implements IStructuredContentProvider {

    public static final String GREEN = "icons/obj16/ok.png";
    public static final String RED = "icons/obj16/nok.png";

    public static final String ATTR_ACKNOWLEDGE_QUEUED_STATUS = "Acknowledge_Queued_Status";
    public static final String ATTR_ACKNOWLEDGE_QUEUED_TIME = "Acknowledge_Queued_Time";
    public static final String ATTR_ACKNOWLEDGE_QUEUED_MESSAGE = "Acknowledge_Queued_Message";
    public static final String ATTR_ACKNOWLEDGE_RELEASED_STATUS = "Acknowledge_Released_Status";
    public static final String ATTR_ACKNOWLEDGE_RELEASED_TIME = "Acknowledge_Released_Time";
    public static final String ATTR_ACKNOWLEDGE_RELEASED_MESSAGE = "Acknowledge_Released_Message";
    public static final String ATTR_ACKNOWLEDGE_SENT_STATUS = "Acknowledge_Sent_Status";
    public static final String ATTR_ACKNOWLEDGE_SENT_TIME = "Acknowledge_Sent_Time";
    public static final String ATTR_ACKNOWLEDGE_SENT_MESSAGE = "Acknowledge_Sent_Message";

    public static final String ACKNOWLEDGE_PREFIX = "Acknowledge_";
    public static final String VERIFIER_PREFIX = "Verifier_";
    public static final String STATUS_SUFFIX = "_Status";
    public static final String TIME_SUFFIX = "_Time";

    public static final String ATTR_TRANSMISSION_CONSTRAINTS_STATUS = "TransmissionConstraints_Status";
    public static final String ATTR_TRANSMISSION_CONSTRAINTS_TIME = "TransmissionConstraints_Time";
    public static final String ATTR_TRANSMISSION_CONSTRAINTS_MESSAGE = "TransmissionConstraints_Message";
    public static final String ATTR_COMMAND_COMPLETE_STATUS = "CommandComplete_Status";
    public static final String ATTR_COMMAND_COMPLETE_TIME = "CommandComplete_Time";
    public static final String ATTR_COMMAND_COMPLETE_MESSAGE = "CommandComplete_Message";
    public static final String ATTR_FINAL_SEQUENCE_COUNT = "Final_Sequence_Count";
    public static final String ATTR_SOURCE = "source";
    public static final String ATTR_BINARY = "binary";
    public static final String ATTR_USERNAME = "username";
    public static final String ATTR_COMMENT = "comment";

    private Map<CommandId, CommandHistoryRecord> recordsByCommandId = new LinkedHashMap<>();
    private TableViewer tableViewer;
    private boolean scrollLock;

    public CommandHistoryRecordContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO could happen when switching channels
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return recordsByCommandId.values().toArray();
    }

    public static String toHumanReadableName(CommandHistoryAttribute attribute) {
        return attribute.getName()
                .replace(ACKNOWLEDGE_PREFIX, "")
                .replace(STATUS_SUFFIX, "")
                .replace(TIME_SUFFIX, "");
    }

    public void addEntries(List<CommandHistoryEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        Collections.reverse(entries);
        for (CommandHistoryEntry entry : entries) {
            processCommandHistoryEntry(entry);
        }
    }

    public void processCommandHistoryEntry(CommandHistoryEntry entry) {
        CommandId commandId = entry.getCommandId();
        CommandHistoryRecord rec;
        boolean create;
        if (recordsByCommandId.containsKey(commandId)) {
            rec = recordsByCommandId.get(commandId);
            create = false;
        } else {
            Instant generationTime = Instant.parse(entry.getGenerationTimeUTC());
            rec = new CommandHistoryRecord(commandId, generationTime);
            recordsByCommandId.put(commandId, rec);
            create = true;
        }

        // Autoprocess attributes for additional columns
        for (CommandHistoryAttribute attr : entry.getAttrList()) {
            String shortName = toHumanReadableName(attr);
            if (attr.getName().startsWith(ACKNOWLEDGE_PREFIX)) {
                if (attr.getName().endsWith(STATUS_SUFFIX)) {
                    rec.addAcknowledgment(new Acknowledgment(rec, shortName, attr));
                    if (attr.getValue().getStringValue().equals("OK")) {
                        rec.addCellImage(shortName, GREEN);
                    } else if (attr.getValue().getStringValue().equals("NOK")) {
                        rec.addCellImage(shortName, RED);
                    }
                } else if (attr.getName().endsWith(TIME_SUFFIX)) {
                    rec.updateAcknowledgmentTime(shortName, attr);
                }
            }

            if (attr.getName().startsWith(VERIFIER_PREFIX)) {
                if (attr.getName().endsWith(STATUS_SUFFIX)) {
                    rec.addAcknowledgment(new Acknowledgment(rec, shortName, attr));
                    if (attr.getValue().getStringValue().equals("OK")) {
                        rec.addCellImage(shortName, GREEN);
                    } else if (attr.getValue().getStringValue().equals("NOK")) {
                        rec.addCellImage(shortName, RED);
                    }
                } else if (attr.getName().endsWith(TIME_SUFFIX)) {
                    rec.updateAcknowledgmentTime(shortName, attr);
                }
            }

            if (attr.getName().equals(ATTR_COMMAND_COMPLETE_STATUS)) {
                if (attr.getValue().getStringValue().equals("OK")) {
                    rec.setCommandState(CommandState.COMPLETED);
                } else {
                    rec.setCommandState(CommandState.FAILED);
                }
            } else if (attr.getName().equals(ATTR_COMMAND_COMPLETE_MESSAGE)) {
                rec.getPTVInfo().setFailureMessage(attr.getValue().getStringValue());
            } else if (attr.getName().equals(ATTR_COMMAND_COMPLETE_TIME)) {
                // Ignore
            } else if (attr.getName().equals("CommandComplete")) { // Legacy
                // Ignore
            } else if (attr.getName().equals(ATTR_FINAL_SEQUENCE_COUNT)) {
                rec.setFinalSequenceCount(attr.getValue());
            } else if (attr.getName().equals(ATTR_SOURCE)) {
                rec.setCommandString(attr.getValue());
            } else if (attr.getName().equals(ATTR_USERNAME)) {
                rec.setUsername(attr.getValue());
            } else if (attr.getName().equals(ATTR_TRANSMISSION_CONSTRAINTS_STATUS)) {
                rec.getPTVInfo().setState(PTVInfo.State.fromYamcsValue(attr.getValue()));
            } else if (attr.getName().equals(ATTR_BINARY)) {
                rec.setBinary(attr.getValue().getBinaryValue());
            } else if (attr.getName().equals(ATTR_COMMENT)) {
                rec.setComment(attr.getValue().getStringValue());
            } else {
                rec.addCellValue(shortName, attr.getValue());
            }
        }

        // All done, make changes visible
        if (create) {
            tableViewer.add(rec);
            maybeSelectAndReveal(rec);
        } else {
            tableViewer.update(rec, null); // Null, means all properties
            maybeSelectAndReveal(rec);
        }
    }

    public void maybeSelectAndReveal(CommandHistoryRecord rec) {
        if (!scrollLock) {
            IStructuredSelection sel = new StructuredSelection(rec);
            tableViewer.setSelection(sel, true);
        }
    }

    public void enableScrollLock(boolean enabled) {
        scrollLock = enabled;
    }

    public int getCommandCount(CommandState commandState) {
        int count = 0;
        for (CommandHistoryRecord rec : recordsByCommandId.values()) {
            if (rec.getCommandState() == commandState) {
                count++;
            }
        }
        return count;
    }

    public void clearAll() {
        // TODO not sure if this is the recommended way to delete all. Need to verify
        BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), () -> {
            tableViewer.getTable().setRedraw(false);
            Collection<CommandHistoryRecord> recs = recordsByCommandId.values();
            tableViewer.remove(recs.toArray());
            recordsByCommandId.clear();
            tableViewer.getTable().setRedraw(true);
        });
    }
}
