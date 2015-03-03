package org.csstudio.platform.libs.yamcs;

import java.util.Collection;

import org.yamcs.protostuff.NamedObjectList;
import org.yamcs.xtce.MetaCommand;

public abstract class MDBContextListener {

    public void onParametersChanged(NamedObjectList parameters) {
    }
    
    public void onCommandsChanged(Collection<MetaCommand> commands) {
    }
}
