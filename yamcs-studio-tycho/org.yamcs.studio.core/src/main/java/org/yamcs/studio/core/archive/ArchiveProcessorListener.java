package org.yamcs.studio.core.archive;

import javax.swing.SwingUtilities;

import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Yamcs.ReplayRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.protobuf.YamcsManagement.TmStatistics;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Listens to processing updates in order to draw the vertical locator bars
 */
public class ArchiveProcessorListener implements ProcessorListener {

    private Display display;
    private DataViewer dataViewer;

    public ArchiveProcessorListener(Display display, DataViewer dataViewer) {
        this.display = display;
        this.dataViewer = dataViewer;
    }

    @Override
    public void processorUpdated(ProcessorInfo processorInfo) {
        if (display.isDisposed())
            return;
        display.asyncExec(() -> {
            ClientInfo clientInfo = YamcsPlugin.getDefault().getClientInfo();
            if (clientInfo != null
                    && processorInfo.getName().equals(clientInfo.getProcessorName())
                    && processorInfo.getInstance().equals(clientInfo.getInstance())) {
                SwingUtilities.invokeLater(() -> {
                    if (processorInfo.hasReplayRequest()) {
                        dataViewer.getDataView().setCurrentLocator(dataViewer.getDataView().DO_NOT_DRAW);
                        ReplayRequest rr = processorInfo.getReplayRequest();
                        dataViewer.getDataView().setStartLocator(rr.getStart());
                        dataViewer.getDataView().setStopLocator(rr.getStop());
                    } else {
                        dataViewer.getDataView().setStartLocator(dataViewer.getDataView().DO_NOT_DRAW);
                        dataViewer.getDataView().setStopLocator(dataViewer.getDataView().DO_NOT_DRAW);
                        dataViewer.getDataView().setCurrentLocator(dataViewer.getDataView().DO_NOT_DRAW);
                    }
                });
            }
        });
    }

    @Override
    public void yProcessorClosed(ProcessorInfo ci) {
    }

    @Override
    public void clientUpdated(ClientInfo ci) {
    }

    @Override
    public void clientDisconnected(ClientInfo ci) {
    }

    @Override
    public void updateStatistics(Statistics stats) {
        if (display.isDisposed())
            return;
        display.asyncExec(() -> {
            if (display.isDisposed())
                return;
            ClientInfo clientInfo = YamcsPlugin.getDefault().getClientInfo();
            if (clientInfo != null
                    && stats.getYProcessorName().equals(clientInfo.getProcessorName())
                    && stats.getInstance().equals(clientInfo.getInstance())) {
                // find the timestamp of the most recent packet received
                SwingUtilities.invokeLater(() -> {
                    long pos = 0;
                    for (TmStatistics ts : stats.getTmstatsList())
                        pos = Math.max(pos, ts.getLastPacketTime());
                    dataViewer.getDataView().setCurrentLocator(pos);
                });
            }
        });
    }
}
