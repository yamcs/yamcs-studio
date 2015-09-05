package org.yamcs.studio.core;

import java.util.List;

import org.yamcs.protobuf.Rest.RestParameter;

public interface MDBContextListener {

    void onParametersChanged(List<RestParameter> parameters);
}
