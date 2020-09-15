package org.yamcs.studio.core.ui;

import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.utils.StatusLineContributionItem;

public class ProcessorStatusLineContributionItem extends StatusLineContributionItem implements YamcsAware {

    private static final String DEFAULT_TEXT = "---";

    public ProcessorStatusLineContributionItem(String id) {
        this(id, CALC_TRUE_WIDTH);
    }

    public ProcessorStatusLineContributionItem(String id, int charWidth) {
        super(id, charWidth);
        setText(DEFAULT_TEXT);
        setToolTipText("Subscribed Yamcs Processor");
        YamcsPlugin.addListener(this);
    }

    @Override
    public void changeProcessorInfo(ProcessorInfo processor) {
        Display display = Display.getDefault();
        if (display.isDisposed()) {
            return;
        }

        display.asyncExec(() -> {
            if (processor == null) {
                setText(DEFAULT_TEXT);
            } else {
                setText(processor.getInstance() + "/" + processor.getName());
            }

            /*if (connectionInfo.hasInstance()) {
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
            }*/
        });
    }

    @Override
    public void dispose() {
        YamcsPlugin.removeListener(this);
    }
}
