package org.csstudio.platform.libs.yamcs.ws;

/**
 * Tag anything to be sent upstream on the websocket. Enables extending classes to
 * declare whether they can 'merge' with the 'next' event (events are queued while
 * there is no connection). This type of behaviour would allow the web-socket client
 * to limit the amount of events sent, while keeping everything interleaved.
 */
public abstract class OutgoingEvent {
    
    public boolean canMergeWith(OutgoingEvent otherEvent) {
        return false;
    }
    
    public OutgoingEvent mergeWith(OutgoingEvent otherEvent) {
        throw new UnsupportedOperationException();
    }
}
