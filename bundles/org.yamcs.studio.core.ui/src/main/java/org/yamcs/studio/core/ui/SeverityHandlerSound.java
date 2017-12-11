package org.yamcs.studio.core.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.yamcs.protobuf.Pvalue.MonitoringResult;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.css.vtype.SeverityHandler;

public class SeverityHandlerSound implements SeverityHandler {

    static String triggerCondition = "NONE";
    static int beepLevel = 0;
    static Map<String, ParameterValue> pvals = new ConcurrentHashMap<>();

    public SeverityHandlerSound() {
        updatePrefence();
    }

    public static void updatePrefence() {
        triggerCondition = YamcsUIPlugin.getDefault().getPreferenceStore()
                .getString("trigerBeep");
        if (YamcsUIPlugin.getDefault().getPreferenceStore()
                .getBoolean("beepWarning")) {
            beepLevel = MonitoringResult.WARNING.getNumber();
        } else {
            beepLevel = MonitoringResult.CRITICAL.getNumber();
        }
        pvals.clear();
    }

    @Override
    public void handle(ParameterValue pval) {
        // Do not beep
        if ("NONE".equals(triggerCondition))
            return;

        // Beep only at the first occurrence of the parameter out-of-limit
        boolean shouldBeep = pval.getMonitoringResult() != null
                && pval.getMonitoringResult().getNumber() >= beepLevel;
        if ("FIRST".equals(triggerCondition)) {
            ParameterValue previousPval = pvals.get(pval.getId().getName());
            if (previousPval != null) {
                pvals.remove(previousPval);
                if (previousPval.getMonitoringResult() != null
                        && pval.getMonitoringResult() != null
                        && previousPval.getMonitoringResult().getNumber() >= pval
                                .getMonitoringResult().getNumber())
                    shouldBeep = false;
            }
            pvals.put(pval.getId().getName(), pval);
        }

        // Beep at each occurrence of the parameter out-of-limit
        if (shouldBeep) {
            // System.out.println("beeping for parameter "
            // + pval.getId().getName());
            SoundSystem.beep();
        }
    }

}
