package org.yamcs.studio.core.ui;

import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ManagementListener;
import org.yamcs.studio.core.ui.utils.StatusLineContributionItem;

/**
 * TODO current processor info should be maintained in ManagementCatalogue, and
 * less iffy As a result we should no longer need to implement the
 * studioconnectionlistener
 */
public class ProcessorStatusLineContributionItem extends StatusLineContributionItem
        implements ManagementListener, StudioConnectionListener {

    private static final String DEFAULT_TEXT = "Not Connected";

    public ProcessorStatusLineContributionItem(String id) {
        this(id, 40);
    }

    public ProcessorStatusLineContributionItem(String id, int charWidth) {
        super(id, charWidth);
        setText(DEFAULT_TEXT);
        setToolTipText("Subscribed Yamcs Processor");
        ManagementCatalogue.getInstance().addManagementListener(this);
        ConnectionManager.getInstance().addStudioConnectionListener(this);
    }

    @Override
    public void dispose() {
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        if (catalogue != null) {
            catalogue.removeManagementListener(this);
        }
        ConnectionManager connManager = ConnectionManager.getInstance();
        if (connManager != null) {
            connManager.removeStudioConnectionListener(this);
        }
    }

    @Override
    public void clientUpdated(ClientInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (updatedInfo.getCurrentClient()) {
                ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
                ProcessorInfo processorInfo = catalogue.getProcessorInfo(updatedInfo.getInstance(),
                        updatedInfo.getProcessorName());
                updateText(processorInfo);
            }
        });
    }

    @Override
    public void clientDisconnected(ClientInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (updatedInfo.getCurrentClient()) {
                updateText(null);
            }
        });
    }

    @Override
    public void processorUpdated(ProcessorInfo updatedInfo) {
    }

    private void updateText(ProcessorInfo processorInfo) {
        if (processorInfo == null) {
            setText(DEFAULT_TEXT);
        } else {
            setText(processorInfo.getInstance() + "/" + processorInfo.getName());
        }
    }

    @Override
    public void processorClosed(ProcessorInfo processorInfo) {
    }

    @Override
    public void statisticsUpdated(Statistics stats) {
    }

    @Override
    public void onStudioConnect() {
    }

    @Override
    public void onStudioDisconnect() {
        updateText(null);
    }
}
