package org.yamcs.studio.core.ui.processor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.protobuf.Rest.EditProcessorRequest;
import org.yamcs.protobuf.Yamcs.ReplaySpeed;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.studio.core.model.ManagementCatalogue;

// TODO should disable button using state while response is not yet in
public class ForwardHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ProcessorInfo processorInfo = ManagementCatalogue.getInstance().getCurrentProcessorInfo();

        String newSpeed;
        if (processorInfo.getReplayRequest().hasSpeed()) {
            ReplaySpeed currentSpeed = processorInfo.getReplayRequest().getSpeed();
            float speedValue = currentSpeed.getParam() * 2f;
            if (speedValue > 17)
                speedValue = 1.0f;
            newSpeed = (speedValue == 0f ? 1f : speedValue) + "x";
        } else {
            newSpeed = "2x";
        }
        EditProcessorRequest req = EditProcessorRequest.newBuilder().setSpeed(newSpeed).build();
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        catalogue.editProcessorRequest(processorInfo.getInstance(), processorInfo.getName(), req);
        return null;
    }
}
