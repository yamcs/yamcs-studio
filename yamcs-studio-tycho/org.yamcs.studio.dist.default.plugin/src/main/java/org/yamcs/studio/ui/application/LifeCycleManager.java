package org.yamcs.studio.ui.application;

import java.util.logging.Logger;

import org.csstudio.autocomplete.AutoCompleteHelper;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ManagementListener;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.studio.ui.processor.ProcessorStateProvider;

@SuppressWarnings("restriction")
public class LifeCycleManager {

    private static final Logger log = Logger.getLogger(LifeCycleManager.class.getName());

    @PostContextCreate
    public void postContextCreate(IEventBroker broker) {
        registerAutocompleteExtensions();

        broker.subscribe(UILifeCycle.APP_STARTUP_COMPLETE, evt -> {

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
            });

            // request connection to Yamcs server
            RCPUtils.runCommand("org.yamcs.studio.ui.autoconnect");
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
