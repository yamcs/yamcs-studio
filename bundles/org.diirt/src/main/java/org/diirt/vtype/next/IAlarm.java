/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * Immutable Alarm implementation.
 *
 * @author carcassi
 */
class IAlarm extends Alarm {

    private final AlarmSeverity severity;
    private final String name;

    IAlarm(AlarmSeverity severity, String name) {
        this.severity = severity;
        this.name = name;
    }

    @Override
    public AlarmSeverity getSeverity() {
        return severity;
    }

    @Override
    public String getName() {
        return name;
    }

}
