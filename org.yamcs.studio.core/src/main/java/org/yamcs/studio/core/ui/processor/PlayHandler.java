package org.yamcs.studio.core.ui.processor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Currently only resumes a paused replay. Should eventually also seek to the beginning and replay a stopped replay. We
 * should probably do this at the server level, rather than stitching it in here.
 */
public class PlayHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ProcessorClient processor = YamcsPlugin.getProcessorClient();
        processor.resume();
        return null;
    }
}
