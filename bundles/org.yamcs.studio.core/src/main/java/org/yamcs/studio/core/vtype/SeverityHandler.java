package org.yamcs.studio.core.vtype;

import org.yamcs.protobuf.Pvalue.ParameterValue;

public interface SeverityHandler {
    void handle(ParameterValue pval);
}
