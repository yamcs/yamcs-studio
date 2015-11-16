package org.yamcs.studio.core.vtype;

import java.text.NumberFormat;

import org.epics.util.text.NumberFormats;
import org.epics.util.time.Timestamp;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VType;
import org.yamcs.protobuf.Mdb.AlarmLevelType;
import org.yamcs.protobuf.Mdb.AlarmRange;
import org.yamcs.protobuf.Pvalue.AcquisitionStatus;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.model.ParameterCatalogue;

public class YamcsVType implements VType, Alarm, Time, Display {
    protected ParameterValue pval;
    public static SeverityHandler severityHandler = null;

    public YamcsVType(ParameterValue pval) {
        this.pval = pval;
        if (severityHandler != null)
            severityHandler.handle(pval);
    }

    @Override
    public AlarmSeverity getAlarmSeverity() {
        if (pval.getAcquisitionStatus() == AcquisitionStatus.EXPIRED)
            return AlarmSeverity.INVALID; // Workaround to display LOS in the displays, should be 'Expired'

        if (!pval.hasMonitoringResult())
            return AlarmSeverity.NONE;

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
        return Timestamp.of(YamcsUTCString.parse(pval.getGenerationTimeUTC()));
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
        ParameterCatalogue catalogue = ParameterCatalogue.getInstance();
        String unit = catalogue.getCombinedUnit(pval.getId());
        return (unit == null) ? "" : unit;
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
        // Assumes ordered ranges
        for (AlarmRange range : pval.getAlarmRangeList()) {
            if (range.getLevel() == AlarmLevelType.WATCH
                    || range.getLevel() == AlarmLevelType.WARNING
                    || range.getLevel() == AlarmLevelType.DISTRESS)
                return range.getMaxInclusive();
        }
        return Double.MAX_VALUE;
    }

    /**
     * Highest value before the alarm region
     */
    @Override
    public Double getUpperAlarmLimit() {
        // Assumes ordered ranges
        for (AlarmRange range : pval.getAlarmRangeList()) {
            if (range.getLevel() == AlarmLevelType.CRITICAL
                    || range.getLevel() == AlarmLevelType.SEVERE)
                return range.getMaxInclusive();
        }
        return Double.MAX_VALUE;
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
