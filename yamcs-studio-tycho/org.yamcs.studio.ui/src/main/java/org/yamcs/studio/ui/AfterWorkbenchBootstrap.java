package org.yamcs.studio.ui;

import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ConnectionFailureListener;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.ui.processor.ProcessingCommandState;

/**
 * Will be activated after the workbench initialized.
 */
public class AfterWorkbenchBootstrap implements IStartup, ProcessorListener, ConnectionFailureListener {

    private static final Logger log = Logger.getLogger(AfterWorkbenchBootstrap.class.getName());

    @Override
    public void earlyStartup() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().asyncExec(() -> {
            doUpdateGlobalProcessingState(workbench, null); // Trigger initial state
                YamcsPlugin.getDefault().addProcessorListener(this); // Further updates (also gives us existing model)
                YamcsPlugin.getDefault().addConnectionFailureListener(this);
            });
    }

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

        if (clientInfo == null)
        {
            int i = 0;
            i++;
        }

        workbench.getDisplay().asyncExec(
                () -> {
                    if (YamcsPlugin.getDefault().getClientInfo() != null && clientInfo != null
                            && clientInfo.getId() == YamcsPlugin.getDefault().getClientInfo().getId()) {
                        ProcessorInfo processorInfo = YamcsPlugin.getDefault().getProcessorInfo(clientInfo.getProcessorName());
                        doUpdateGlobalProcessingState(workbench, processorInfo);
                    }
                });
    }

    private void doUpdateGlobalProcessingState(IWorkbench workbench, ProcessorInfo processorInfo) {
        // TODO Remove this log warning if #1 is no longer occurring
        if (workbench.isStarting())
            log.warning("debug issue #1: You shouldn't see this message inside an asyncExec");

        ISourceProviderService service = (ISourceProviderService) workbench.getService(ISourceProviderService.class);
        ProcessingCommandState state = (ProcessingCommandState) service.getSourceProvider(ProcessingCommandState.STATE_KEY_PROCESSING);
        state.updateState(processorInfo);
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
