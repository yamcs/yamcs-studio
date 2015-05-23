package org.yamcs.studio.ui;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IEvaluationService;
import org.yamcs.studio.core.YamcsLoginModule;
import org.yamcs.studio.core.YamcsPlugin;

public class ConnectHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(ConnectHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try
        {
            // Check if authentication is needed
            if (!YamcsLoginModule.isAuthenticationNeeded())
            {
                YamcsPlugin.getDefault().connect(null);
                return null;
            }

            // Authentication is needed, call the org.csstudio.security.ui login command
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
            IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
            try {
                Command cmd = commandService.getCommand("org.csstudio.security.login");
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } catch (Exception ex)
        {
            log.log(Level.SEVERE, "", ex);
        }
        return null;
    }
}
