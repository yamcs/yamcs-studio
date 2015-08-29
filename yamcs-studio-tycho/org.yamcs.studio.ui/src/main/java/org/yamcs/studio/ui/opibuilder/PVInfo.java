package org.yamcs.studio.ui.opibuilder;

import org.csstudio.simplepv.IPV;
import org.yamcs.protobuf.Rest.RestParameterInfo;

public class PVInfo implements Comparable<PVInfo> {

    private String displayName;
    private IPV pv;

    // In case it's a yamcs parameter, one of these two should be filled in
    private RestParameterInfo parameterInfo;
    private String parameterInfoException;

    public PVInfo(String displayName, IPV pv) {
        this.displayName = displayName;
        this.pv = pv;
    }

    public RestParameterInfo getParameterInfo() {
        return parameterInfo;
    }

    public String getPVType() {
        if (displayName.startsWith("sw://"))
            return "Yamcs Software Parameter";
        else if (displayName.startsWith("para://"))
            return "Yamcs Parameter";
        else if (displayName.startsWith("loc://"))
            return "Local PV";
        else if (displayName.startsWith("sim://"))
            return "Locally Simulated PV";
        else if (displayName.startsWith("sys://"))
            return "Local System Indicator";
        else if (displayName.startsWith("="))
            return "Formula";
        else if (isYamcsParameter())
            return "Yamcs Parameter";
        else
            return "Unknown";
    }

    public void setParameterInfo(RestParameterInfo restParameterInfo) {
        this.parameterInfo = restParameterInfo;
    }

    public void setParameterInfoException(String message) {
        parameterInfoException = message;
    }

    public String getParameterInfoException() {
        return parameterInfoException;
    }

    public String getDisplayName() {
        return displayName;
    }

    public IPV getPV() {
        return pv;
    }

    public boolean isYamcsParameter() {
        return !displayName.contains("://")
                || displayName.startsWith("sw://")
                || displayName.startsWith("para://");
    }

    @Override
    public int compareTo(PVInfo other) {
        return displayName.compareTo(other.displayName);
    }

    public String getYamcsQualifiedName() {
        if (!isYamcsParameter())
            throw new UnsupportedOperationException();

        if (displayName.startsWith("sw://"))
            return displayName.substring(5);
        else if (displayName.startsWith("para://"))
            return displayName.substring(7);
        else
            return displayName;
    }
}
