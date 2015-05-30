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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.ISourceProviderService;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ConnectionFailureListener;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.ui.handlers.ConnectHandler;
import org.yamcs.studio.ui.processor.ProcessorStateProvider;

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

                // Listen to processing-info updates
                doUpdateGlobalProcessingState(PlatformUI.getWorkbench(), null); // Trigger initial state
                YamcsPlugin.getDefault().addProcessorListener(new ProcessorListener() {
                    @Override
                    public void processorUpdated(ProcessorInfo processorInfo) {
                        updateGlobalProcessingState(processorInfo);
                    }

                    @Override
                    public void yProcessorClosed(ProcessorInfo processorInfo) {
                        updateGlobalProcessingState(processorInfo);
                    }

                    @Override
                    public void updateStatistics(Statistics stats) {
                    }

                    @Override
                    public void clientUpdated(ClientInfo clientInfo) {
                        updateGlobalProcessingState(clientInfo);
                    }

                    @Override
                    public void clientDisconnected(ClientInfo clientInfo) {
                        updateGlobalProcessingState(clientInfo);
                    }
                });

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
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Could not switch node", e);
                }
            });
        }
        else
        {
            YamcsPlugin.getDefault().abortSwitchNode();
        }
    }

    private void updateGlobalProcessingState(ProcessorInfo processorInfo) {
        // First update state of various buttons (at the level of the workbench)
        // (TODO sometimes clientInfo has not been updated yet, that's whey we have the next method too)
        IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().asyncExec(() -> {
            ClientInfo clientInfo = YamcsPlugin.getDefault().getClientInfo();
            if (clientInfo != null && clientInfo.getProcessorName().equals(processorInfo.getName())) {
                doUpdateGlobalProcessingState(workbench, processorInfo);
            }
        });
    }

    private void updateGlobalProcessingState(ClientInfo clientInfo) {
        // TODO Not sure which one of this method or the previous would trigger first, and whether that's deterministic
        // therefore, just have similar logic here.
        IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().asyncExec(() -> {
            if (YamcsPlugin.getDefault().getClientInfo() != null && clientInfo != null
                    && clientInfo.getId() == YamcsPlugin.getDefault().getClientInfo().getId()) {
                ProcessorInfo processorInfo = YamcsPlugin.getDefault().getProcessorInfo(clientInfo.getProcessorName());
                doUpdateGlobalProcessingState(workbench, processorInfo);
            }
        });
    }

    private void doUpdateGlobalProcessingState(IWorkbench workbench, ProcessorInfo processorInfo) {
        ISourceProviderService service = (ISourceProviderService) workbench.getService(ISourceProviderService.class);
        ProcessorStateProvider state = (ProcessorStateProvider) service.getSourceProvider(ProcessorStateProvider.STATE_KEY_PROCESSING);
        state.updateState(processorInfo);
    }
}
