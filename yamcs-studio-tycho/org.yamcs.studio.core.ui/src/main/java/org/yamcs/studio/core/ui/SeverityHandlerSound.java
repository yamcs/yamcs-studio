package org.yamcs.studio.core.ui;

import org.epics.vtype.AlarmSeverity;
import org.yamcs.studio.core.vtype.SeverityHandler;
import org.yamcs.studio.core.vtype.YamcsVType;

public class SeverityHandlerSound implements SeverityHandler {

	@Override
	public void handle(YamcsVType pval) {
		if (pval.getAlarmSeverity() == AlarmSeverity.MINOR
				|| pval.getAlarmSeverity() == AlarmSeverity.MAJOR) {
			SoundSystem.beep();
		}
	}
}
