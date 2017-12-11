package org.yamcs.studio.css.vtype;

import org.yamcs.protobuf.Pvalue.ParameterValue;

public interface SeverityHandler {
    void handle(ParameterValue pval);
}
