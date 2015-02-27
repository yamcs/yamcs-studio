package org.csstudio.vtype.pv.yamcs;

import org.csstudio.vtype.pv.PV;
import org.csstudio.vtype.pv.PVFactory;

public class Yamcs_PVFactory implements PVFactory {
    public final static String TYPE = "yamcs";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public PV createPV(String name, String baseName) throws Exception {
        return new Yamcs_PV(name, baseName);
    }
}
