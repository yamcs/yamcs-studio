package org.yamcs.studio.ui.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.runmode.IOPIRuntime;
import org.csstudio.opibuilder.runmode.OPIView;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest.Operation;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.processor.SwitchProcessorDialog;

import com.google.protobuf.MessageLite;

public class SwitchProcessorHandler extends AbstractRestHandler {

    private static final Logger log = Logger.getLogger(SwitchProcessorHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (!checkRestClient(event, "switch processor"))
            return null;

        Shell shell = HandlerUtil.getActiveShellChecked(event);
        SwitchProcessorDialog dialog = new SwitchProcessorDialog(shell);
        if (dialog.open() == Window.OK) {
            ProcessorInfo info = dialog.getProcessorInfo();
            if (info != null) {
                resetDisplays();

                ProcessorManagementRequest req = ProcessorManagementRequest.newBuilder()
                        .setOperation(Operation.CONNECT_TO_PROCESSOR)
                        .setInstance(info.getInstance())
                        .setName(info.getName())
                        .addClientId(YamcsPlugin.getDefault().getClientInfo().getId()).build();
                restClient.createProcessorManagementRequest(req, new ResponseHandler() {
                    @Override
                    public void onMessage(MessageLite responseMsg) {
                        Display.getDefault().asyncExec(() -> {
                            YamcsPlugin.getDefault().refreshClientInfo();
                        });
                    }

                    @Override
                    public void onException(Exception e) {
                        log.log(Level.SEVERE, "Could not switch processor", e);
                    }
                });
            }
        }

        return null;
    }

    private void resetDisplays() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            for (IWorkbenchPage page : window.getPages()) {
                for (IViewReference reference : page.getViewReferences()) {
                    IViewPart viewPart = reference.getView(false);
                    if (viewPart instanceof IOPIRuntime)
                        refreshDisplay((IOPIRuntime) viewPart);
                }
                for (IEditorReference reference : page.getEditorReferences()) {
                    IEditorPart editorPart = reference.getEditor(false);
                    if (editorPart instanceof IOPIRuntime)
                        refreshDisplay((IOPIRuntime) editorPart);
                }
            }
        }
    }

    private void refreshDisplay(IOPIRuntime opiRuntime) {
        try {
            OPIView.ignoreMemento();
            opiRuntime.setOPIInput(opiRuntime.getOPIInput());
        } catch (PartInitException e) {
            ErrorHandlerUtil.handleError("Failed to refresh OPI", e);
        }
    }
}
