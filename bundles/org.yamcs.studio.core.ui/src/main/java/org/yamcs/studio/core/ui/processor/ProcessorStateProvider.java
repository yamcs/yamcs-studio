package org.yamcs.studio.core.ui.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;

/**
 * Used in plugin.xml core-expressions to keep track of play/pause button state
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ProcessorStateProvider extends AbstractSourceProvider {

    private static final Logger log = Logger.getLogger(ProcessorStateProvider.class.getName());

    public static final String STATE_KEY_PROCESSING = "org.yamcs.studio.core.ui.processor.state.processing";
    public static final String STATE_KEY_REPLAY = "org.yamcs.studio.core.ui.processor.state.replay";
    public static final String STATE_KEY_REPLAY_SPEED = "org.yamcs.studio.core.ui.processor.state.speed";
    private static final String[] SOURCE_NAMES = { STATE_KEY_REPLAY, STATE_KEY_PROCESSING, STATE_KEY_REPLAY_SPEED };

    private boolean replay = false;
    private String processing = "";
    private float speed = 1;

    public void updateState(ProcessorInfo processorInfo) {
        if (processorInfo == null) {
            replay = false;
            processing = "";
            speed = 1;
        } else {
            replay = processorInfo.hasReplayRequest();
            processing = replay ? processorInfo.getReplayState().toString() : "";
            speed = replay ? processorInfo.getReplayRequest().getSpeed().getParam() : 1;
        }

        Map newState = getCurrentState();
        log.fine(String.format("Fire new processing state %s", newState));
        fireSourceChanged(ISources.WORKBENCH, newState);
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap(3);
        map.put(STATE_KEY_REPLAY, replay);
        map.put(STATE_KEY_PROCESSING, processing);
        map.put(STATE_KEY_REPLAY_SPEED, speed);
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
