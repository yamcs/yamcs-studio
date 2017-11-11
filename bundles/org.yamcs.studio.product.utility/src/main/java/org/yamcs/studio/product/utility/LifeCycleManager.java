package org.yamcs.studio.product.utility;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.ISourceProviderService;
import org.yamcs.protobuf.Web.ConnectionInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ManagementListener;
import org.yamcs.studio.core.ui.YamcsUIPlugin;
import org.yamcs.studio.core.ui.connections.ConnectionPreferences;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.studio.ui.processor.ProcessorStateProvider;

@SuppressWarnings("restriction")
public class LifeCycleManager {

    private static final Logger log = Logger.getLogger(LifeCycleManager.class.getName());

    @PostContextCreate
    public void postContextCreate(IEventBroker broker) {
        broker.subscribe(UILifeCycle.APP_STARTUP_COMPLETE, evt -> {

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
            IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
            try {
                // This is a bit of a hack. Have an unknown problem with the toolbar disappearing after
                // workbench restart. Below code does a double 'toggle' on it, to make it appear again.
                Command cmd = commandService.getCommand("org.eclipse.ui.ToggleCoolbarAction");
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));
            } catch (Exception exception) {
                log.log(Level.SEVERE, "Could not execute command", exception);
            }

            // Listen to processing-info updates
            doUpdateGlobalProcessingState(PlatformUI.getWorkbench(), null); // Trigger initial state
            ManagementCatalogue.getInstance().addManagementListener(new ManagementListener() {
                @Override
                public void processorUpdated(ProcessorInfo processorInfo) {
                    updateGlobalProcessingState(processorInfo);
                }

                @Override
                public void processorClosed(ProcessorInfo processorInfo) {
                    updateGlobalProcessingState(processorInfo);
                }

                @Override
                public void statisticsUpdated(Statistics stats) {
                }

                @Override
                public void clientUpdated(ClientInfo clientInfo) {
                    updateGlobalProcessingState(clientInfo);
                }

                @Override
                public void clientDisconnected(ClientInfo clientInfo) {
                    updateGlobalProcessingState(clientInfo);
                }
                
                @Override
                public void instanceUpdated(ConnectionInfo connectionInfo) {
                }

                @Override
                public void clearAllManagementData() {
                }
            });

            // request connection to Yamcs server
            boolean singleConnectionMode = YamcsUIPlugin.getDefault().getPreferenceStore().getBoolean("singleConnectionMode");
            if(!singleConnectionMode && ConnectionPreferences.isAutoConnect()) {
                RCPUtils.runCommand("org.yamcs.studio.ui.autoconnect");
            }
        });
    }

    private void updateGlobalProcessingState(ProcessorInfo processorInfo) {
        // First update state of various buttons (at the level of the workbench)
        // (TODO sometimes clientInfo has not been updated yet, that's whey we have the next method too)
        IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().asyncExec(() -> {
            ClientInfo clientInfo = ManagementCatalogue.getInstance().getCurrentClientInfo();
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
            if (clientInfo.getCurrentClient()) {
                ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
                ProcessorInfo processorInfo = catalogue.getProcessorInfo(clientInfo.getProcessorName());
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
