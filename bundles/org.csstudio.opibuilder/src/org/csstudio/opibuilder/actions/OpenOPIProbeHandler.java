package org.csstudio.opibuilder.actions;

import java.util.LinkedHashMap;
import java.util.Optional;

import org.csstudio.csdata.ProcessVariable;
import org.csstudio.opibuilder.runmode.RunModeService;
import org.csstudio.opibuilder.runmode.RunModeService.DisplayMode;
import org.csstudio.opibuilder.util.MacrosInput;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.ui.util.AdapterUtil;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenOPIProbeHandler extends AbstractHandler {

    private static final String MACRO_NAME = "probe_pv";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getActiveMenuSelection(event);
        ProcessVariable[] pvs = AdapterUtil.convert(selection, ProcessVariable.class);

        IPath probeOPIPath = ResourceUtil.getPathFromString("platform:/plugin/org.csstudio.opibuilder/opi/probe.opi");

        LinkedHashMap<String, String> macros = new LinkedHashMap<>();
        if (pvs.length > 0) {
            macros.put(MACRO_NAME, pvs[0].getName());
        }

        int i = 0;
        for (ProcessVariable pv : pvs) {
            macros.put(MACRO_NAME + "_" + Integer.toString(i), pv.getName());
            i++;
        }

        MacrosInput macrosInput = new MacrosInput(macros, true);

        // Errors in here will show in dialog and error log
        RunModeService.openDisplay(probeOPIPath, Optional.of(macrosInput), DisplayMode.NEW_TAB_DETACHED,
                Optional.empty());
        return null;
    }
}
