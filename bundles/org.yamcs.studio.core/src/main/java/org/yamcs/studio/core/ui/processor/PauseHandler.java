package org.yamcs.studio.core.ui.processor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.studio.core.YamcsPlugin;

public class PauseHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ProcessorClient processor = YamcsPlugin.getProcessorClient();
        processor.pause();
        return null;
    }
}
