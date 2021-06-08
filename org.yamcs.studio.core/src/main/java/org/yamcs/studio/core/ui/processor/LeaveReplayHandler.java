package org.yamcs.studio.core.ui.processor;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.studio.core.RemoteEntityHolder;
import org.yamcs.studio.core.YamcsPlugin;

public class LeaveReplayHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(LeaveReplayHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        YamcsClient yamcsClient = YamcsPlugin.getYamcsClient();
        yamcsClient.listProcessors(YamcsPlugin.getInstance()).whenComplete((processors, err) -> {
            if (err == null) {
                ProcessorInfo defaultProcessor = processors.stream()
                        .filter(processor -> processor.getPersistent() && !processor.getReplay())
                        .findFirst()
                        .get();

                RemoteEntityHolder holder = new RemoteEntityHolder();
                holder.yamcsClient = YamcsPlugin.getYamcsClient();
                holder.serverInfo = YamcsPlugin.getServerInfo();
                holder.userInfo = YamcsPlugin.getUser();
                holder.missionDatabase = YamcsPlugin.getMissionDatabase();
                holder.instance = YamcsPlugin.getInstance();
                holder.processor = defaultProcessor;
                log.info(String.format("Switching to '%s' processor (instance: %s)", defaultProcessor.getName(),
                        defaultProcessor.getInstance()));
                YamcsPlugin.updateEntities(holder);
            }
        });

        return null;
    }
}
