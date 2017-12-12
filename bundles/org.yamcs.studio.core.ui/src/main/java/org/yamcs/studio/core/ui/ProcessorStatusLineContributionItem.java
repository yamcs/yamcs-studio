package org.yamcs.studio.core.ui;

import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Web.ConnectionInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.protobuf.YamcsManagement.YamcsInstance;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ManagementListener;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.studio.core.ui.utils.StatusLineContributionItem;

/**
 * TODO current processor info should be maintained in ManagementCatalogue, and
 * less iffy As a result we should no longer need to implement the
 * studioconnectionlistener
 */
public class ProcessorStatusLineContributionItem extends StatusLineContributionItem
        implements ManagementListener, StudioConnectionListener {
    
    private static final Logger log = Logger.getLogger(ProcessorStatusLineContributionItem.class.getName());

    private static final String DEFAULT_TEXT = "---";

    public ProcessorStatusLineContributionItem(String id) {
        this(id, CALC_TRUE_WIDTH);
    }

    public ProcessorStatusLineContributionItem(String id, int charWidth) {
        super(id, charWidth);
        setText(DEFAULT_TEXT);
        setToolTipText("Subscribed Yamcs Processor");
        addClickListener(evt -> {
            if (ConnectionManager.getInstance().isConnected()) {
                // Hmm should probably move processor plugin back in core.ui
                RCPUtils.runCommand("org.yamcs.studio.processor.infoCommand");
            }
        });
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
    
    @Override
    public void instanceUpdated(ConnectionInfo connectionInfo) {
        Display.getDefault().asyncExec(() -> {
            YamcsInstance instance = connectionInfo.getInstance();
            String baseText = instance.getName(); // TODO don't get processor??
            switch (instance.getState()) {
            case NEW:
            case STARTING:
                // setText("Starting " + instance.getName()); // TODO text currently managed by processorInfo events
                break;
            case STOPPING:
                setErrorText(baseText + " (stopping...)", null);
                break;
            case TERMINATED:
                setErrorText(baseText + " (terminated)", null);
                break;
            case FAILED:
                String detail = (instance.hasFailureCause() ? instance.getFailureCause() : null);
                setErrorText(baseText + " (start failure)", detail);
                break;
            case RUNNING:
                setErrorText(null, null);
                break;
            default:
                log.warning("Unexpected instance state " + instance.getState());
                setErrorText(null, null);
            }
        });
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
    public void clearAllManagementData() {
    }

    @Override
    public void onStudioConnect() {
    }

    @Override
    public void onStudioDisconnect() {
        Display display = Display.getDefault();
        if (!display.isDisposed()) {
            display.asyncExec(() -> updateText(null));
        }
    }
}
