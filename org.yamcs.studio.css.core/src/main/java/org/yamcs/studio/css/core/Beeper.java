package org.yamcs.studio.css.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.yamcs.protobuf.Pvalue.MonitoringResult;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.css.core.prefs.SoundCommandHandler;
import org.yamcs.studio.data.yamcs.YamcsSubscriptionService.ParameterValueListener;

public class Beeper implements ParameterValueListener {

    private String triggerCondition = "NONE";
    private int beepLevel = 0;
    private Map<NamedObjectId, MonitoringResult> pastResults = new ConcurrentHashMap<>();

    public Beeper() {
        updatePreference();
    }

    public void updatePreference() {
        var plugin = Activator.getDefault();

        triggerCondition = plugin.getPreferenceStore().getString("triggerBeep");
        if (plugin.getPreferenceStore().getBoolean("beepWarning")) {
            beepLevel = MonitoringResult.WARNING.getNumber();
        } else {
            beepLevel = MonitoringResult.CRITICAL.getNumber();
        }
        pastResults.clear();

        // update toolbar icon
        try {
            SoundCommandHandler.beep = triggerCondition;
            var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            var commandService = window.getService(ICommandService.class);
            if (commandService != null) {
                commandService.refreshElements("dropdownSoundCommand", null);
            }
        } catch (Exception e) {
            // might pass here at startup when the workbench is not created yet
        }
    }

    @Override
    public void onData(List<ParameterValue> values) {
        if ("NONE".equals(triggerCondition)) {
            return;
        }

        var beep = false;
        for (ParameterValue pval : values) {
            if (!pval.hasMonitoringResult()) {
                continue;
            }

            if (pval.getMonitoringResult().getNumber() >= beepLevel) {
                // Beep only at the first occurrence of the parameter out-of-limit
                if ("FIRST".equals(triggerCondition)) {
                    var prevResult = pastResults.get(pval.getId());
                    if (prevResult == null || (prevResult.getNumber() < pval.getMonitoringResult().getNumber())) {
                        beep = true;
                    }
                } else {
                    beep = true;
                }
            }
            pastResults.put(pval.getId(), pval.getMonitoringResult());
        }

        if (beep) {
            YamcsPlugin.getSoundSystem().beep();
        }
    }
}
