package org.yamcs.studio.ui.application;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.autocomplete.AutoCompleteHelper;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IEvaluationService;
import org.yamcs.studio.core.ConnectionFailureListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.ui.ConnectHandler;

@SuppressWarnings("restriction")
public class LifeCycleManager implements ConnectionFailureListener {

    private static final Logger log = Logger.getLogger(LifeCycleManager.class.getName());

    @PostContextCreate
    public void postContextCreate(IEventBroker broker) {
        registerAutocompleteExtensions();

        YamcsPlugin.getDefault().addConnectionFailureListener(this);

        broker.subscribe(UILifeCycle.APP_STARTUP_COMPLETE, evt -> {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
            IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
            try {
                // This is about as hacky as it gets. Have an unknown problem with the toolbar disappearing after
                // workbench restart. Below code does a double 'toggle' on it, to make it appear again.
                // TODO Find exact reason why. In previous commit this did not appear to be a problem (?)
                Command cmd = commandService.getCommand("org.eclipse.ui.ToggleCoolbarAction");
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));

                // request connection to Yamcs server
                (new ConnectHandler()).execute(null);
            } catch (Exception exception) {
                log.log(Level.SEVERE, "Could not execute command", exception);
            }
        });
    }

    /**
     * This is a bit of a hack to get yamcs datasources registered early on. Maybe there's a better
     * way, but couldn't find it right away.
     */
    private void registerAutocompleteExtensions() {
        StringBuilder msg = new StringBuilder("Registering datasources early on: ");
        for (String prefix : AutoCompleteHelper.retrievePVManagerSupported()) {
            msg.append(prefix + "://   ");
        }
        log.fine(msg.toString());
    }

    @Override
    public void connectionFailure(int currentNode, int nextNode) {
        Display.getDefault().asyncExec(() -> {
            askSwitchNode(currentNode, nextNode);
        });
    }

    private static void askSwitchNode(int currentNode, int nextNode) {
        MessageDialog dialog = new MessageDialog(null, "Connection Lost", null, "Connection to Yamcs Server node " + currentNode + " is lost.\n\n" +
                "Would you like to switch connection to node " + nextNode + " now?",
                MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
        if (dialog.open() == 0)
        {
            Display.getDefault().asyncExec(() -> {
                YamcsPlugin.getDefault().disconnect();
                try {
                    YamcsPlugin.getDefault().switchNode(nextNode);
                    ;
                } catch (Exception e) {
                }
            });
        }
        else
        {
            YamcsPlugin.getDefault().abortSwitchNode();
        }
    }
}
