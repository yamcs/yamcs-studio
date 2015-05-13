package org.yamcs.studio.ui.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;

/**
 * Used in core-expressions define defined in plugin.xml to keep track of play/pause button state
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ProcessingCommandState extends AbstractSourceProvider {

    private static final Logger log = Logger.getLogger(ProcessingCommandState.class.getName());

    public static final String STATE_KEY_REPLAY = "org.yamcs.studio.ui.processor.state.replay";
    public static final String STATE_KEY_PROCESSING = "org.yamcs.studio.ui.processor.state.processing";
    private static final String[] SOURCE_NAMES = { STATE_KEY_REPLAY, STATE_KEY_PROCESSING };

    private boolean replay = false;
    private String processing = "";

    public void updateState(ProcessorInfo processorInfo) {
        if (processorInfo == null) {
            replay = false;
            processing = "";
        } else {
            replay = processorInfo.hasReplayRequest();
            processing = replay ? processorInfo.getReplayState().toString() : "";
        }

        Map newState = getCurrentState();
        log.info(String.format("Fire new processing state %s", newState));
        fireSourceChanged(ISources.WORKBENCH, newState);
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap(2);
        map.put(STATE_KEY_REPLAY, replay);
        map.put(STATE_KEY_PROCESSING, processing);
        return map;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return SOURCE_NAMES;
    }

    @Override
    public void dispose() {
    }
}
