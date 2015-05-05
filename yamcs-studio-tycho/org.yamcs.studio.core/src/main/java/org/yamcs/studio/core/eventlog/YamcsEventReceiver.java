package org.yamcs.studio.core.eventlog;

import static org.yamcs.api.Protocol.decode;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.MessageHandler;
import org.hornetq.api.core.client.SessionFailureListener;
import org.yamcs.YamcsException;
import org.yamcs.api.ConnectionListener;
import org.yamcs.api.YamcsClient;
import org.yamcs.api.YamcsConnector;
import org.yamcs.protobuf.Yamcs.Event;

public class YamcsEventReceiver implements ConnectionListener, MessageHandler, SessionFailureListener {
    private static final Logger log = Logger.getLogger(YamcsEventReceiver.class.getName());
    EventLogView eventView;
    YamcsConnector yconnector;
    YamcsClient yamcsClient;

    public YamcsEventReceiver(YamcsConnector yconnector, EventLogView eventView) {
        this.yconnector = yconnector;
        this.eventView = eventView;
        yconnector.addConnectionListener(this);
    }

    @Override
    public void onMessage(ClientMessage msg) {
        try {
            Event ev = (Event) decode(msg, Event.newBuilder());
            eventView.addEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connected(String url) {
        try {
            yamcsClient = yconnector.getSession().newClientBuilder()
                    .setDataConsumer(new SimpleString(yconnector.getConnectionParams().getInstance() + ".events_realtime"), null).build();
            yamcsClient.dataConsumer.setMessageHandler(this);
        } catch (HornetQException e) {
            log.log(Level.SEVERE, "Could not set-up HornetQ connection", e);
        }
    }

    @Override
    public void beforeReconnect(HornetQException arg0) {
        //should not be called because reconnection is not configured in the factory
    }

    public void retrievePastEvents() {
        // TODO
        /*
         * PastEventParams params = YarchPastEventsDialog.showDialog(eventView); if (params.ok) {
         * new Thread(new EventDumpReceiver(yconnector, params, eventView)).start(); }
         */
    }

    @Override
    public void connectionFailed(HornetQException exception, boolean failedOver) {
    }

    @Override
    public void connecting(String url) {
    }

    @Override
    public void connectionFailed(String url, YamcsException exception) {
    }

    @Override
    public void disconnected() {
    }

    @Override
    public void log(String message) {
    }
}
