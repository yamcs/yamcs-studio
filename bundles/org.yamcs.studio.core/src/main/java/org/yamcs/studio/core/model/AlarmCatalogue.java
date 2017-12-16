package org.yamcs.studio.core.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Alarms.AlarmData;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsClient;

public class AlarmCatalogue implements Catalogue {

    private Set<AlarmListener> alarmListeners = new CopyOnWriteArraySet<>();

    private Map<String, AlarmData> alarmDataByName = new ConcurrentHashMap<>();

    public static AlarmCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(AlarmCatalogue.class);
    }

    @Override
    public void onStudioConnect() {
        YamcsClient yamcsClient = ConnectionManager.getInstance().getYamcsClient();
        yamcsClient.sendMessage(new WebSocketRequest("alarms", "subscribe"));
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        // TODO
    }

    @Override
    public void onStudioDisconnect() {
        // Clear state
        alarmDataByName.clear();
    }

    public void addAlarmListener(AlarmListener listener) {
        alarmListeners.add(listener);

        // Inform of current model
        alarmDataByName.values().forEach(alarmData -> listener.processAlarmData(alarmData));
    }

    public void removeAlarmListener(AlarmListener listener) {
        alarmListeners.remove(listener);
    }

    public void processAlarmData(AlarmData alarmData) {
        alarmListeners.forEach(l -> l.processAlarmData(alarmData));
    }
}
