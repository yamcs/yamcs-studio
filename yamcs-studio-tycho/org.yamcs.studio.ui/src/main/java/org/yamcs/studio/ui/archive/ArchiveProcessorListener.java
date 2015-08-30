package org.yamcs.studio.ui.archive;

import javax.swing.SwingUtilities;

import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Yamcs.ReplayRequest;
import org.yamcs.protobuf.Yamcs.TimeInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.protobuf.YamcsManagement.TmStatistics;
import org.yamcs.studio.core.ManagementCatalogue;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.TimeListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.web.RestClient;

/**
 * Listens to processing updates in order to draw the vertical locator bars
 */
public class ArchiveProcessorListener implements StudioConnectionListener, ProcessorListener, TimeListener {

    private Display display;
    private DataViewer dataViewer;

    public ArchiveProcessorListener(Display display, DataViewer dataViewer) {
        this.display = display;
        this.dataViewer = dataViewer;
    }

    @Override
    public void onStudioConnect(YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        if (webSocketClient != null) {
            webSocketClient.addTimeListener(this);
        }
    }

    @Override
    public void onStudioDisconnect() {
        SwingUtilities.invokeLater(() -> {
            dataViewer.getDataView().setCurrentLocator(dataViewer.getDataView().DO_NOT_DRAW);
        });
    }

    @Override
    public void processTime(TimeInfo timeInfo) {
        SwingUtilities.invokeLater(() -> {
            dataViewer.getDataView().setCurrentLocator(timeInfo.getCurrentTime());
        });
    }

    @Override
    public void processorUpdated(ProcessorInfo processorInfo) {
        if (display.isDisposed())
            return;
        display.asyncExec(() -> {
            ClientInfo clientInfo = ManagementCatalogue.getInstance().getCurrentClientInfo();
            if (clientInfo != null
                    && processorInfo.getName().equals(clientInfo.getProcessorName())
                    && processorInfo.getInstance().equals(clientInfo.getInstance())) {
                SwingUtilities.invokeLater(() -> {
                    if (processorInfo.hasReplayRequest()) {
                        ReplayRequest rr = processorInfo.getReplayRequest();
                        dataViewer.getDataView().setStartLocator(rr.getStart());
                        dataViewer.getDataView().setStopLocator(rr.getStop());
                    } else {
                        dataViewer.getDataView().setStartLocator(dataViewer.getDataView().DO_NOT_DRAW);
                        dataViewer.getDataView().setStopLocator(dataViewer.getDataView().DO_NOT_DRAW);
                    }
                });
            }
        });
    }

    @Override
    public void processorClosed(ProcessorInfo ci) {
    }

    @Override
    public void clientUpdated(ClientInfo ci) {
    }

    @Override
    public void clientDisconnected(ClientInfo ci) {
    }

    @Override
    public void statisticsUpdated(Statistics stats) {
        if (display.isDisposed())
            return;
        display.asyncExec(() -> {
            if (display.isDisposed())
                return;
            ClientInfo clientInfo = ManagementCatalogue.getInstance().getCurrentClientInfo();
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
