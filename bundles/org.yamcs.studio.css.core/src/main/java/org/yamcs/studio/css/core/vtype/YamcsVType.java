package org.yamcs.studio.css.core.vtype;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.Date;

import org.diirt.util.NumberFormats;
import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.Display;
import org.diirt.vtype.Time;
import org.diirt.vtype.VType;
import org.yamcs.protobuf.Mdb.AlarmLevelType;
import org.yamcs.protobuf.Mdb.AlarmRange;
import org.yamcs.protobuf.Pvalue.AcquisitionStatus;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.css.core.pvmanager.PVConnectionInfo;

public class YamcsVType implements VType, Alarm, Time, Display {
    protected ParameterValue pval;

    public YamcsVType(ParameterValue pval) {
        this.pval = pval;
    }

    @Override
    public AlarmSeverity getAlarmSeverity() {
        if (pval.getAcquisitionStatus() == AcquisitionStatus.EXPIRED
                || pval.getAcquisitionStatus() == AcquisitionStatus.NOT_RECEIVED
                || pval.getAcquisitionStatus() == AcquisitionStatus.INVALID)
            return AlarmSeverity.INVALID; // Workaround to display LOS in the displays, should be 'Expired'

        if (!pval.hasMonitoringResult())
            return AlarmSeverity.NONE;

        switch (pval.getMonitoringResult()) {
        case IN_LIMITS:
            return AlarmSeverity.NONE;
        case DISABLED:
            return AlarmSeverity.NONE;
        case WATCH:
        case WARNING:
        case DISTRESS:
            return AlarmSeverity.MINOR;
        case CRITICAL:
        case SEVERE:
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
    public Instant getTimestamp() {
        Date dt = YamcsUTCString.parse(pval.getGenerationTimeUTC());
        return (dt != null) ? dt.toInstant() : null;
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
    public static YamcsVType fromYamcs(PVConnectionInfo info, ParameterValue pval) {
        if (pval.hasEngValue()
                && info != null
                && info.parameter != null
                && info.parameter.getType() != null
                && "enumeration".equals(info.parameter.getType().getEngType())) {
            return new EnumeratedVType(info, pval);
        } else {
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
                throw new IllegalStateException(
                        "Unexpected type for parameter value. Got: " + pval.getEngValue().getType());
            }
        }
    }
}
