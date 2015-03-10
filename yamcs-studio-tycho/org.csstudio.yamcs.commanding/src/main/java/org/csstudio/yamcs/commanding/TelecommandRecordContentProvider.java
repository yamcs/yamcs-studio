package org.csstudio.yamcs.commanding;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.yamcs.protostuff.CommandHistoryAttribute;
import org.yamcs.protostuff.CommandHistoryEntry;
import org.yamcs.protostuff.CommandId;

public class TelecommandRecordContentProvider implements IStructuredContentProvider {

    public static final String GREEN = "icons/ok.png";
    public static final String RED = "icons/nok.png";
    
    private static final String ACKNOWLEDGE_PREFIX = "Acknowledge_";
    private static final String STATUS_SUFFIX = "_Status";
    private static final String TIME_SUFFIX = "_Time";
    
    private Map<CommandId, TelecommandRecord> recordsByCommandId = new LinkedHashMap<>();
    private TableViewer tableViewer;
    
    public TelecommandRecordContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        throw new UnsupportedOperationException(); // TODO could happen when switching channels
    }
    
    @Override
    public void dispose() {
    }
    
    @Override
    public Object[] getElements(Object inputElement) {
       return recordsByCommandId.values().toArray();
    }

    public void processCommandHistoryEntry(CommandHistoryEntry entry) {
        CommandId commandId = entry.getCommandId();
        TelecommandRecord rec;
        boolean create;
        if (recordsByCommandId.containsKey(commandId)) {
            rec = recordsByCommandId.get(commandId);
            create = false;
        } else {
            // These attributes 'should' be there
            String source = commandId.getCommandName(); // In case there is no source
            String username = "anonymous";
            String finalSequenceCount = ""; 
            for (CommandHistoryAttribute attr : entry.getAttrList()) {
                if ("source".equals(attr.getName())) {
                    source = attr.getValue().getStringValue();
                } else if ("username".equals(attr.getName())) {
                    username = attr.getValue().getStringValue();
                } else if ("Final_Sequence_Count".equals(attr.getName())) {
                    finalSequenceCount = attr.getValue().getStringValue();
                }
            }
            
            rec = new TelecommandRecord(commandId, source, username, finalSequenceCount);
            recordsByCommandId.put(commandId, rec);
            create = true;
        }
        
        
        // Autoprocess attributes for additional columns
        for (CommandHistoryAttribute attr : entry.getAttrList()) {
            String shortName = attr.getName()
                    .replace(ACKNOWLEDGE_PREFIX, "")
                    .replace(STATUS_SUFFIX, "")
                    .replace(TIME_SUFFIX, "");
            if (attr.getName().endsWith(STATUS_SUFFIX)) {
                if (attr.getValue().getStringValue().contains("OK")) {
                    rec.addCellImage(shortName, GREEN);
                } else {
                    rec.addCellImage(shortName, RED);
                }
            } else {
                rec.addCellValue(shortName, attr.getValue());
            }
        }
        
        // All done, make changes visible
        if (create) {
            tableViewer.add(rec);
        } else {
            tableViewer.update(rec, null); // Null, means all properties
        }
    }
}
