package org.yamcs.studio.links;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.yamcs.protobuf.LinkInfo;

public class LinkTableViewerContentProvider implements IStructuredContentProvider {

    // name -> rec
    private Map<String, LinkRecord> linksByName = new HashMap<>();

    private TableViewer tableViewer;

    public LinkTableViewerContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        Collection<LinkRecord> records = linksByName.values();
        return (records != null) ? records.toArray() : new Object[0];
    }

    public void processLinkInfo(LinkInfo incoming) {
        LinkRecord rec = linksByName.get(incoming.getName());
        if (rec == null) {
            rec = new LinkRecord(incoming);
            linksByName.put(incoming.getName(), rec);
            tableViewer.add(rec);
        } else {
            rec.processIncomingLinkInfo(incoming);
            tableViewer.update(rec, null);
        }
    }

    public void clearView() { // Clears the view. Not the internal data
        // TODO not sure if this is the recommended way to delete all. Need to verify
        BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), () -> {
            tableViewer.getTable().setRedraw(false);
            Collection<LinkRecord> recs = linksByName.values();
            tableViewer.remove(recs.toArray());
            tableViewer.getTable().setRedraw(true);
        });
    }

    public void clearAll() {
        // TODO not sure if this is the recommended way to delete all. Need to verify
        BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), () -> {
            tableViewer.getTable().setRedraw(false);
            Collection<LinkRecord> recs = linksByName.values();
            tableViewer.remove(recs.toArray());
            linksByName.clear();
            tableViewer.getTable().setRedraw(true);
        });
    }
}
