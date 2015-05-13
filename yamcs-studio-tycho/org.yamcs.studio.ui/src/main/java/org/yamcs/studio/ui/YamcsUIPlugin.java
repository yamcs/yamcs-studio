package org.yamcs.studio.ui;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.ISourceProviderService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.ui.processor.ProcessingCommandState;
import org.yamcs.utils.TimeEncoding;

public class YamcsUIPlugin extends AbstractUIPlugin implements ProcessorListener {

    public static final String PLUGIN_ID = "org.yamcs.studio.core.ui";

    // The shared instance
    private static YamcsUIPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        TimeEncoding.setUp();

        YamcsPlugin.getDefault().addProcessorListener(this);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static YamcsUIPlugin getDefault() {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(String path) {
        Bundle bundle = FrameworkUtil.getBundle(YamcsPlugin.class);
        return ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path(path), null));
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
        Display.getDefault().asyncExec(() -> {
            ClientInfo clientInfo = YamcsPlugin.getDefault().getClientInfo();
            if (clientInfo.getProcessorName().equals(processorInfo.getName())) {
                doUpdateGlobalProcessingState(processorInfo);
            }
        });
    }

    private void updateGlobalProcessingState(ClientInfo clientInfo) {
        // TODO Not sure which one of this method or the previous would trigger first, and whether that's deterministic
        // therefore, just have similar logic here.
        Display.getDefault().asyncExec(() -> {
            if (clientInfo.getId() == YamcsPlugin.getDefault().getClientInfo().getId()) {
                ProcessorInfo processorInfo = YamcsPlugin.getDefault().getProcessorInfo(clientInfo.getProcessorName());
                doUpdateGlobalProcessingState(processorInfo);
            }
        });
    }

    private void doUpdateGlobalProcessingState(ProcessorInfo processorInfo) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        ISourceProviderService service = (ISourceProviderService) workbench.getService(ISourceProviderService.class);
        ProcessingCommandState state = (ProcessingCommandState) service.getSourceProvider(ProcessingCommandState.STATE_KEY_PROCESSING);
        state.updateState(processorInfo);
    }
}
