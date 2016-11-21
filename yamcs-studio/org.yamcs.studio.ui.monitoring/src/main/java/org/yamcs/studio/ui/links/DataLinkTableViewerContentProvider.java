package org.yamcs.studio.ui.links;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.yamcs.protobuf.YamcsManagement.LinkInfo;

/**
 * Manages data for all links (regardless of which instance). Based on external configuration, it
 * will switch the content of the associated table viewer to only show links for a specific Yamcs
 * instance.
 */
public class DataLinkTableViewerContentProvider implements IStructuredContentProvider {

    // instance -> name -> rec
    private Map<String, Map<String, DataLinkRecord>> linksByNameByInstance = new HashMap<>();

    // Instance for which currently to show all links
    private String yamcsInstance;

    private TableViewer tableViewer;

    public DataLinkTableViewerContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    /**
     * Sets the toggle that will determine which of the known links will be show in the view. Also
     * ensures a full refresh of the table.
     */
    public void processYamcsInstance(String yamcsInstance) {
        this.yamcsInstance = yamcsInstance;
        tableViewer.refresh();
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (yamcsInstance == null) {
            return new Object[0];
        } else {
            Map<String, DataLinkRecord> linksByName = linksByNameByInstance.get(yamcsInstance);
            if (linksByName == null)
                return new Object[0];
            Collection<DataLinkRecord> records = linksByName.values();
            return (records != null) ? records.toArray() : new Object[0];
        }
    }

    public void processLinkInfo(LinkInfo incoming) {
        String incomingInstance = incoming.getInstance();
        Map<String, DataLinkRecord> linksByName = linksByNameByInstance.get(incomingInstance);
        if (linksByName == null) {
            linksByName = new LinkedHashMap<>();
            linksByNameByInstance.put(incomingInstance, linksByName);
        }

        DataLinkRecord rec = linksByName.get(incoming.getName());
        if (rec == null) {
            rec = new DataLinkRecord(incoming);
            linksByName.put(incoming.getName(), rec);
            if (incomingInstance.equals(yamcsInstance)) {
                tableViewer.add(rec);
            }
        } else {
            rec.processIncomingLinkInfo(incoming);
            if (incomingInstance.equals(yamcsInstance)) {
                tableViewer.update(rec, null);
            }
        }
    }

    public void clearView() { // Clears the view. Not the internal data
        // TODO not sure if this is the recommended way to delete all. Need to verify
        BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), () -> {
            tableViewer.getTable().setRedraw(false);
            Collection<DataLinkRecord> recs = linksByNameByInstance.get(yamcsInstance).values();
            tableViewer.remove(recs.toArray());
            tableViewer.getTable().setRedraw(true);
        });
    }

    public void clearAll() {
        // TODO not sure if this is the recommended way to delete all. Need to verify
        BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), () -> {
            tableViewer.getTable().setRedraw(false);
            Collection<DataLinkRecord> recs = linksByNameByInstance.get(yamcsInstance).values();
            tableViewer.remove(recs.toArray());
            linksByNameByInstance.clear();
            tableViewer.getTable().setRedraw(true);
        });
    }
}
