package org.yamcs.studio.core.ui.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.Yamcs.ReplaySpeed.ReplaySpeedType;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Used in plugin.xml core-expressions to keep track of play/pause button state
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ProcessorStateProvider extends AbstractSourceProvider implements YamcsAware {

    private static final Logger log = Logger.getLogger(ProcessorStateProvider.class.getName());

    public static final String STATE_KEY_NAME = "org.yamcs.studio.core.ui.processor.state.name";
    public static final String STATE_KEY_PROCESSING = "org.yamcs.studio.core.ui.processor.state.processing";
    public static final String STATE_KEY_REPLAY = "org.yamcs.studio.core.ui.processor.state.replay";
    public static final String STATE_KEY_REPLAY_SPEED = "org.yamcs.studio.core.ui.processor.state.speed";

    private static final String[] SOURCE_NAMES = {
            STATE_KEY_NAME, STATE_KEY_REPLAY, STATE_KEY_PROCESSING, STATE_KEY_REPLAY_SPEED
    };

    private String name = "";
    private boolean replay = false;
    private String processing = "";
    private float speed = 1;

    public ProcessorStateProvider() {
        YamcsPlugin.addListener(this);
    }

    @Override
    public void changeProcessorInfo(ProcessorInfo processor) {
        Display.getDefault().asyncExec(() -> {
            if (processor == null) {
                name = "";
                replay = false;
                processing = "";
                speed = 1;
            } else {
                name = processor.getName();
                replay = processor.hasReplayRequest();
                processing = replay ? processor.getReplayState().toString() : "";
                if (replay) {
                    if (processor.getReplayRequest().getSpeed().getType() == ReplaySpeedType.STEP_BY_STEP) {
                        speed = -1;
                    } else {
                        speed = processor.getReplayRequest().getSpeed().getParam();
                    }
                } else {
                    speed = 1;
                }
            }

            Map newState = getCurrentState();
            log.fine(String.format("Fire new processing state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap(4);
        map.put(STATE_KEY_NAME, name);
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
        YamcsPlugin.removeListener(this);
    }
}
