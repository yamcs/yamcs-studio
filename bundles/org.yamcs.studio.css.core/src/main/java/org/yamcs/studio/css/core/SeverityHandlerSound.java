package org.yamcs.studio.css.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.yamcs.protobuf.Pvalue.MonitoringResult;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.ParameterListener;
import org.yamcs.studio.core.ui.SoundSystem;

public class SeverityHandlerSound implements ParameterListener {

    static String triggerCondition = "NONE";
    static int beepLevel = 0;
    static Map<String, ParameterValue> pvals = new ConcurrentHashMap<>();

    public SeverityHandlerSound() {
        updatePrefence();
        ParameterCatalogue.getInstance().addParameterListener(this);
    }

    public static void updatePrefence() {
        Activator plugin = Activator.getDefault();

        triggerCondition = plugin.getPreferenceStore().getString("triggerBeep");
        if (plugin.getPreferenceStore().getBoolean("beepWarning")) {
            beepLevel = MonitoringResult.WARNING.getNumber();
        } else {
            beepLevel = MonitoringResult.CRITICAL.getNumber();
        }
        pvals.clear();
    }

    @Override
    public void onParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
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

    @Override
    public void mdbUpdated() {
        pvals.clear();
    }
}
