package org.yamcs.studio.script;

import org.yamcs.protobuf.Mdb;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.yamcs.YamcsVType;

/**
 * MDB Info for a parameter
 */
public class ParameterInfo {

    public String name;
    public String qualifiedName;
    public String shortDescription;
    public String longDescription;
    public String dataSource;
    public String units;
    public String type;
    public String rawType;

    private Mdb.ParameterInfo pinfo;

    public ParameterInfo(YamcsVType value) {
        var mdb = YamcsPlugin.getMissionDatabase();
        if (mdb != null) {
            pinfo = mdb.getParameterInfo(value.getId());
            if (pinfo != null) {
                name = pinfo.getName();
                qualifiedName = pinfo.getQualifiedName();

                if (pinfo.hasShortDescription()) {
                    shortDescription = pinfo.getShortDescription();
                }
                if (pinfo.hasLongDescription()) {
                    longDescription = pinfo.getLongDescription();
                }
                if (pinfo.hasDataSource()) {
                    dataSource = pinfo.getDataSource().name();
                }
                units = mdb.getEngineeringUnits(value.getId());

                if (pinfo.getType().hasEngType()) {
                    type = pinfo.getType().getEngType();
                }
                if (pinfo.getType().getDataEncoding().hasType()) {
                    rawType = pinfo.getType().getDataEncoding().getType().name().toLowerCase();
                }
            }
        }
    }

    public String getAlias(String namespace) {
        if (pinfo != null) {
            for (var alias : pinfo.getAliasList()) {
                if (alias.getNamespace().equals(namespace)) {
                    return alias.getName();
                }
            }
        }
        return null;
    }
}
