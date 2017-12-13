package org.yamcs.studio.core.ui.processor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.protobuf.Rest.EditProcessorRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;

public class PauseHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        ProcessorInfo processorInfo = catalogue.getCurrentProcessorInfo();
        EditProcessorRequest req = EditProcessorRequest.newBuilder().setState("PAUSED").build();
        catalogue.editProcessorRequest(processorInfo.getInstance(), processorInfo.getName(), req);
        return null;
    }
}
