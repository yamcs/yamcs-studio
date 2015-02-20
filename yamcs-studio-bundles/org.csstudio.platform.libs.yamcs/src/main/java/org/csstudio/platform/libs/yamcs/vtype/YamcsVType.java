package org.csstudio.platform.libs.yamcs.vtype;

import java.text.NumberFormat;

import org.epics.util.text.NumberFormats;
import org.epics.util.time.Timestamp;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.yamcs.protobuf.ParameterValue;

public class YamcsVType implements Alarm, Time, Display {
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
            System.out.println("hum no alarms?");
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
        return Timestamp.now(); // TODO
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
        if (pval.getWatchHigh() != null) {
            return pval.getWatchHigh();
        } else if (pval.getWarningHigh() != null) {
            return pval.getWarningHigh();
        } else if (pval.getDistressHigh() != null) {
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
        if (pval.getCriticalHigh() != null) {
            return pval.getCriticalHigh();
        } else if (pval.getSevereHigh() != null) {
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
}
