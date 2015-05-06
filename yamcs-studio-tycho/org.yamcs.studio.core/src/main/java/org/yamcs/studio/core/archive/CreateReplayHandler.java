package org.yamcs.studio.core.archive;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

import com.google.protobuf.MessageLite;

public class CreateReplayHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(CreateReplayHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        SwingUtilities.invokeLater(() -> {
            ArchiveView view = (ArchiveView) part;
            Selection sel = view.archivePanel.getDataViewer().getDataView().getSelection();
            TimeInterval interval = new TimeInterval(sel.getStartInstant(), sel.getStopInstant());
            List<String> packets = view.archivePanel.getSelectedPackets("tm");
            Display.getDefault().asyncExec(() -> {
                CreateReplayDialog dialog = new CreateReplayDialog(Display.getCurrent().getActiveShell());
                dialog.initialize(interval, packets);
                if (dialog.open() == Window.OK) {
                    ProcessorManagementRequest req = dialog.toProcessorManagementRequest();
                    RestClient restClient = YamcsPlugin.getDefault().getRestClient();
                    restClient.createProcessorRequest(req, new ResponseHandler() {
                        @Override
                        public void onMessage(MessageLite responseMsg) {
                            if (responseMsg instanceof RestExceptionMessage) {
                                log.log(Level.WARNING, "Exception returned by server: " + responseMsg);
                            }
                        }

                        @Override
                        public void onException(Exception e) {
                            log.log(Level.SEVERE, "Could not fetch available yamcs parameters", e);
                        }
                    });
                }
            });
        });

        return null;
    }
}
