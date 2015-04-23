package org.yamcs.studio.core.vtype.pv;

import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVFactory;

public class Para_PVFactory implements PVFactory {
    public final static String TYPE = "para";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public PV createPV(String name, String baseName) throws Exception {
        return new Para_PV(name, baseName);
    }
}
