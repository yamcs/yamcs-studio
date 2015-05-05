package org.yamcs.studio.core.eventlog;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.client.ClientMessage;
import org.yamcs.api.Protocol;
import org.yamcs.api.YamcsClient;
import org.yamcs.api.YamcsConnector;
import org.yamcs.protobuf.Yamcs.EndAction;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.EventReplayRequest;
import org.yamcs.protobuf.Yamcs.ReplayRequest;
import org.yamcs.protobuf.Yamcs.StringMessage;
import org.yamcs.utils.TimeEncoding;

public class EventDumpReceiver implements Runnable {

    private static final Logger log = Logger.getLogger(EventDumpReceiver.class.getName());
    private YamcsConnector yconnector;
    private EventLogView eventView;
    private PastEventParams params;

    EventDumpReceiver(YamcsConnector yconnector, PastEventParams params, EventLogView eventView) {
        this.yconnector = yconnector;
        this.eventView = eventView;
        this.params = params;
    }

    @Override
    public void run() {
        try {
            YamcsClient msgClient = yconnector.getSession().newClientBuilder().setRpc(true).setDataConsumer(null, null).build();

            EventReplayRequest err = EventReplayRequest.newBuilder().build();
            ReplayRequest crr = ReplayRequest.newBuilder().setStart(params.start).setStop(params.stop).
                    setEndAction(EndAction.QUIT).setEventRequest(err).build();

            SimpleString replayServer = Protocol.getYarchReplayControlAddress(yconnector.getConnectionParams().getInstance());
            StringMessage answer = (StringMessage) msgClient.executeRpc(replayServer, "createReplay", crr, StringMessage.newBuilder());
            SimpleString replayAddress = new SimpleString(answer.getMessage());
            log.info("Retrieving archived events from " + TimeEncoding.toString(params.start) + " to " + TimeEncoding.toString(params.stop));
            msgClient.executeRpc(replayAddress, "START", null, null);
            int count = 0;
            //send events to the event viewer in batches, otherwise the ui might choke
            List<Event> events = new ArrayList<Event>();
            while (true) {
                ClientMessage msg = msgClient.dataConsumer.receive(1000);
                if (msg == null) {
                    if (!events.isEmpty()) {
                        eventView.addEvents(events);
                        events = new ArrayList<>();
                    }
                    continue;
                }
                if (Protocol.endOfStream(msg)) {
                    if (!events.isEmpty()) {
                        eventView.addEvents(events);
                    }
                    break;
                }
                count++;
                events.add((Event) Protocol.decode(msg, Event.newBuilder()));
                if (events.size() >= 1000) {
                    eventView.addEvents(events);
                    events = new ArrayList<>();
                }
            }
            msgClient.close();
            log.info("Archive retrieval finished, retrieved " + count + " events");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while retrieving archived events", e);
        }
    }
}
