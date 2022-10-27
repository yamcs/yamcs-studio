/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.processor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.Yamcs.EndAction;
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
    public static final String STATE_KEY_PROTECTED = "org.yamcs.studio.core.ui.processor.state.protected";

    public static final String STATE_KEY_REPLAY = "org.yamcs.studio.core.ui.processor.state.replay";
    public static final String STATE_KEY_REPLAY_LOOP = "org.yamcs.studio.core.ui.processor.state.loop";
    public static final String STATE_KEY_REPLAY_SPEED = "org.yamcs.studio.core.ui.processor.state.speed";
    public static final String STATE_KEY_REPLAY_START = "org.yamcs.studio.core.ui.processor.state.start";
    public static final String STATE_KEY_REPLAY_STOP = "org.yamcs.studio.core.ui.processor.state.stop";

    private static final String[] SOURCE_NAMES = {
            STATE_KEY_NAME,
            STATE_KEY_REPLAY,
            STATE_KEY_PROCESSING,
            STATE_KEY_PROTECTED,
            STATE_KEY_REPLAY_LOOP,
            STATE_KEY_REPLAY_SPEED,
            STATE_KEY_REPLAY_START,
            STATE_KEY_REPLAY_STOP,
    };

    private String name = "";
    private boolean protected_ = false;
    private boolean replay = false;
    private String processing = "";
    private float speed = 1;
    private boolean loop = false;
    private String start = "";
    private String stop = "";

    public ProcessorStateProvider() {
        YamcsPlugin.addListener(this);
    }

    @Override
    public void changeProcessorInfo(ProcessorInfo processor) {
        Display.getDefault().asyncExec(() -> {
            name = "";
            processing = "";
            protected_ = false;
            replay = false;
            speed = 1;
            loop = false;
            start = "";
            stop = "";
            if (processor != null) {
                name = processor.getName();
                processing = replay ? processor.getReplayState().toString() : "";
                protected_ = processor.getProtected();
                replay = processor.hasReplayRequest();
                if (replay) {
                    var replayRequest = processor.getReplayRequest();
                    if (replayRequest.hasStart()) {
                        var gpbTimestamp = replayRequest.getStart();
                        start = Instant.ofEpochSecond(gpbTimestamp.getSeconds(), gpbTimestamp.getNanos()).toString();
                    }
                    if (replayRequest.hasStop()) {
                        var gpbTimestamp = replayRequest.getStop();
                        stop = Instant.ofEpochSecond(gpbTimestamp.getSeconds(), gpbTimestamp.getNanos()).toString();
                    }
                    loop = replayRequest.getEndAction() == EndAction.LOOP;
                    if (replayRequest.getSpeed().getType() == ReplaySpeedType.STEP_BY_STEP) {
                        speed = -1;
                    } else {
                        speed = replayRequest.getSpeed().getParam();
                    }
                }
            }

            var newState = getCurrentState();
            log.fine(String.format("Fire new processing state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap();
        map.put(STATE_KEY_NAME, name);
        map.put(STATE_KEY_REPLAY, replay);
        map.put(STATE_KEY_PROCESSING, processing);
        map.put(STATE_KEY_PROTECTED, protected_);
        map.put(STATE_KEY_REPLAY_LOOP, loop);
        map.put(STATE_KEY_REPLAY_SPEED, speed);
        map.put(STATE_KEY_REPLAY_START, start);
        map.put(STATE_KEY_REPLAY_STOP, stop);
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
