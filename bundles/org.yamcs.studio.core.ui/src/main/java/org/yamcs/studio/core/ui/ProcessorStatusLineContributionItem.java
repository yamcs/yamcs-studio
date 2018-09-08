package org.yamcs.studio.core.ui;

import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Web.ConnectionInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.protobuf.YamcsManagement.YamcsInstance;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ManagementListener;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.studio.core.ui.utils.StatusLineContributionItem;

/**
 * TODO current processor info should be maintained in ManagementCatalogue, and less iffy As a result we should no
 * longer need to implement the studioconnectionlistener
 */
public class ProcessorStatusLineContributionItem extends StatusLineContributionItem
        implements ManagementListener, YamcsConnectionListener {

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
            if (YamcsPlugin.getYamcsClient().isConnected()) {
                // Hmm should probably move processor plugin back in core.ui
                RCPUtils.runCommand("org.yamcs.studio.core.ui.processor.infoCommand");
            }
        });
        ManagementCatalogue.getInstance().addManagementListener(this);
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
    }

    @Override
    public void dispose() {
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        if (catalogue != null) {
            catalogue.removeManagementListener(this);
        }
        YamcsPlugin.getDefault().removeYamcsConnectionListener(this);
    }

    @Override
    public void clientUpdated(ClientInfo updatedInfo) {
    }

    @Override
    public void clientDisconnected(ClientInfo updatedInfo) {
    }

    @Override
    public void processorUpdated(ProcessorInfo updatedInfo) {
    }

    @Override
    public void instanceUpdated(ConnectionInfo connectionInfo) {
        Display.getDefault().asyncExec(() -> {
            if (connectionInfo.hasProcessor()) {
                updateText(connectionInfo.getProcessor());
            } else {
                updateText(null);
            }

            YamcsInstance instance = connectionInfo.getInstance();
            String baseText = instance.getName(); // TODO don't get processor??
            switch (instance.getState()) {
            case INITIALIZING:
            case INITIALIZED:
            case STARTING:
                // setText("Starting " + instance.getName()); // TODO text currently managed by processorInfo events
                break;
            case STOPPING:
                setErrorText(baseText + " (stopping...)", null);
                break;
            case OFFLINE:
                setErrorText(baseText + " (offline)", null);
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
    public void statisticsUpdated(Statistics stats) {
    }

    @Override
    public void clearAllManagementData() {
    }

    @Override
    public void onYamcsConnected() {
    }

    @Override
    public void onYamcsDisconnected() {
        if (isDisposed()) {
            return;
        }
        Display.getDefault().asyncExec(() -> updateText(null));
    }
}
