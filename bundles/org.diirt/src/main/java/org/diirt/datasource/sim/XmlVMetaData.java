/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.sim;

import javax.xml.bind.annotation.XmlAttribute;
import org.diirt.vtype.Alarm;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.Time;

/**
 *
 * @author carcassi
 */
class XmlVMetaData extends ReplayValue implements Time, Alarm {

    @XmlAttribute
    Integer timeUserTag;
    @XmlAttribute
    AlarmSeverity alarmSeverity;
    @XmlAttribute
    String alarmName;

    @Override
    public Integer getTimeUserTag() {
        return timeUserTag;
    }

    @Override
    public AlarmSeverity getAlarmSeverity() {
        return alarmSeverity;
    }

    @Override
    public String getAlarmName() {
        return alarmName;
    }

    @Override
    public boolean isTimeValid() {
        return true;
    }

}
