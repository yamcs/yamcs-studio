package org.csstudio.platform.libs.yamcs.ws;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.yamcs.protostuff.NamedObjectId;
import org.yamcs.protostuff.NamedObjectList;

public class ParameterUnsubscribeEvent extends OutgoingEvent {
    
    private Set<NamedObjectId> ids = new HashSet<>();

    public ParameterUnsubscribeEvent(NamedObjectList idList) {
        ids.addAll(idList.getListList());
    }
    
    public NamedObjectList getIdList() {
        NamedObjectList idList = new NamedObjectList();
        idList.setListList(new ArrayList<>(ids));
        return idList;
    }
    
    @Override
    public boolean canMergeWith(OutgoingEvent otherEvent) {
        return otherEvent instanceof ParameterUnsubscribeEvent;
    }
    
    @Override
    public OutgoingEvent mergeWith(OutgoingEvent otherEvent) {
        Set<NamedObjectId> otherIds = ((ParameterUnsubscribeEvent) otherEvent).ids;
        ids.addAll(otherIds);
        return this;
    }
}
