package org.yamcs.studio.css.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.yamcs.protobuf.Pvalue.MonitoringResult;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.ui.SoundSystem;
import org.yamcs.studio.css.core.prefs.SoundCommandHandler;

public class SeverityHandlerSound {

    static String triggerCondition = "NONE";
    static int beepLevel = 0;
    static Map<String, ParameterValue> pvals = new ConcurrentHashMap<>();

    public SeverityHandlerSound() {
        updatePrefence();
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

        // update toolbar icon
        try {
            SoundCommandHandler.beep = triggerCondition;
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
            if (commandService != null) {
                commandService.refreshElements("dropdownSoundCommand", null);
            }
        } catch (Exception e) {
            // might pass here at startup when the workbench is not created yet
        }
    }

    public void onParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            // Do not beep
            if ("NONE".equals(triggerCondition)) {
                return;
            }

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
                                    .getMonitoringResult().getNumber()) {
                        shouldBeep = false;
                    }
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

    /*@Override
    public void mdbUpdated() {
        pvals.clear();
    }*/
}
