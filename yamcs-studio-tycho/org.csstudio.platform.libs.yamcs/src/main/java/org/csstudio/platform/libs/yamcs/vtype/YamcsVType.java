package org.csstudio.platform.libs.yamcs.vtype;

import java.text.NumberFormat;

import org.epics.util.text.NumberFormats;
import org.epics.util.time.Timestamp;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VType;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class YamcsVType implements VType, Alarm, Time, Display {
    protected ParameterValue pval;

    public YamcsVType(ParameterValue pval) {
        this.pval = pval;
    }

    @Override
    public AlarmSeverity getAlarmSeverity() {
        switch (pval.getMonitoringResult()) {
        case IN_LIMITS:
            return AlarmSeverity.NONE;
        case DISABLED:
            return AlarmSeverity.UNDEFINED;
        case WATCH:
        case WATCH_HIGH:
        case WATCH_LOW:
        case WARNING:
        case WARNING_HIGH:
        case WARNING_LOW:
        case DISTRESS:
        case DISTRESS_HIGH:
        case DISTRESS_LOW:
            return AlarmSeverity.MINOR;
        case CRITICAL:
        case CRITICAL_HIGH:
        case CRITICAL_LOW:
        case SEVERE:
        case SEVERE_HIGH:
        case SEVERE_LOW:
            return AlarmSeverity.MAJOR;
        default:
            throw new IllegalStateException("Unexpected alarm severity " + pval.getMonitoringResult());
        }
    }

    @Override
    public String getAlarmName() {
        return "";
    }

    @Override
    public Timestamp getTimestamp() {
        return Timestamp.of(YamcsUTCString.parse(pval.getAcquisitionTimeUTC()));
    }

    @Override
    public Integer getTimeUserTag() {
        return null;
    }

    @Override
    public boolean isTimeValid() {
        return true;
    }

    @Override
    public Double getLowerDisplayLimit() {
        return Double.MIN_VALUE;
    }

    @Override
    public Double getLowerCtrlLimit() {
        return Double.MIN_VALUE;
    }

    @Override
    public Double getLowerAlarmLimit() {
        return Double.MIN_VALUE;
    }

    @Override
    public Double getLowerWarningLimit() {
        return Double.MIN_VALUE;
    }

    @Override
    public String getUnits() {
        return "";
    }

    @Override
    public NumberFormat getFormat() {
        return NumberFormats.toStringFormat();
    }

    /**
     * Highest value before the warning region
     */
    @Override
    public Double getUpperWarningLimit() {
        if (pval.hasWatchHigh()) {
            return pval.getWatchHigh();
        } else if (pval.hasWarningHigh()) {
            return pval.getWarningHigh();
        } else if (pval.hasDistressHigh()) {
            return pval.getDistressHigh();
        } else {
            return Double.MAX_VALUE;
        }
    }

    /**
     * Highest value before the alarm region
     */
    @Override
    public Double getUpperAlarmLimit() {
        if (pval.hasCriticalHigh()) {
            return pval.getCriticalHigh();
        } else if (pval.hasSevereHigh()) {
            return pval.getSevereHigh();
        } else {
            return Double.MAX_VALUE;
        }
    }

    @Override
    public Double getUpperCtrlLimit() {
        return Double.MAX_VALUE;
    }

    @Override
    public Double getUpperDisplayLimit() {
        return Double.MAX_VALUE;
    }

    /**
     * Converts a yamcs ParameterValue to a VType.
     */
    public static YamcsVType fromYamcs(ParameterValue pval) {
        switch (pval.getEngValue().getType()) {
        case UINT32:
            return new Uint32VType(pval);
        case SINT32:
            return new Sint32VType(pval);
        case UINT64:
            return new Uint64VType(pval);
        case SINT64:
            return new Sint64VType(pval);
        case FLOAT:
            return new FloatVType(pval);
        case DOUBLE:
            return new DoubleVType(pval);
        case BOOLEAN:
            return new BooleanVType(pval);
        case STRING:
            return new StringVType(pval);
        case BINARY:
            return new BinaryVType(pval);
        case TIMESTAMP:
            throw new UnsupportedOperationException("No support for timestamp pvals");
        default:
            throw new IllegalStateException("Unexpected type for parameter value. Got: " + pval.getEngValue().getType());
        }
    }
}
