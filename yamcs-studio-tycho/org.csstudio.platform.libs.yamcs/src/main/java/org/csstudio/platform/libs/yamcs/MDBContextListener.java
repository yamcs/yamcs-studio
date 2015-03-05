package org.csstudio.platform.libs.yamcs;

import java.util.Collection;
import java.util.List;

import org.yamcs.protostuff.NamedObjectId;
import org.yamcs.xtce.MetaCommand;

public abstract class MDBContextListener {

    public void onParametersChanged(List<NamedObjectId> parameterIds) {
    }
    
    public void onCommandsChanged(Collection<MetaCommand> commandIds) {
    }
}
