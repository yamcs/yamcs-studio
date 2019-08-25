package org.yamcs.studio.core.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.client.WebSocketClientCallback;
import org.yamcs.client.WebSocketRequest;
import org.yamcs.protobuf.AlarmSubscriptionRequest;
import org.yamcs.protobuf.Alarms.AlarmData;
import org.yamcs.protobuf.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsStudioClient;

public class AlarmCatalogue implements Catalogue, WebSocketClientCallback {

    private Set<AlarmListener> alarmListeners = new CopyOnWriteArraySet<>();

    private Map<String, AlarmData> alarmDataByName = new ConcurrentHashMap<>();

    public static AlarmCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(AlarmCatalogue.class);
    }

    @Override
    public void onYamcsConnected() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();

        AlarmSubscriptionRequest options = AlarmSubscriptionRequest.newBuilder()
                .setDetail(true)
                .build();
        yamcsClient.subscribe(new WebSocketRequest("alarms", "subscribe", options), this);
    }

    @Override
    public void onMessage(WebSocketSubscriptionData msg) {
        if (msg.hasAlarmData()) {
            AlarmData alarmData = msg.getAlarmData();
            alarmListeners.forEach(l -> l.processAlarmData(alarmData));
        }
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        // TODO
    }

    @Override
    public void onYamcsDisconnected() {
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
}
