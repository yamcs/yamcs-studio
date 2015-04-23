package org.yamcs.studio.core;

import java.util.Collection;
import java.util.List;

import org.yamcs.protobuf.Rest.RestParameter;
import org.yamcs.xtce.MetaCommand;

public abstract class MDBContextListener {

    public void onParametersChanged(List<RestParameter> parameters) {
    }

    public void onCommandsChanged(Collection<MetaCommand> commandIds) {
    }
}
