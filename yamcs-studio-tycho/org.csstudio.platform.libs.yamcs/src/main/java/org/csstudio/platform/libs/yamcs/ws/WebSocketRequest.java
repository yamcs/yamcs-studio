package org.csstudio.platform.libs.yamcs.ws;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.protostuff.JsonIOUtil;
import io.protostuff.Message;
import io.protostuff.Schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tags anything to be sent upstream on an established websocket. Enables extending classes to
 * declare whether they can 'merge' with the 'next' event (events are queued while
 * there is no connection). This type of behaviour would allow the web-socket client
 * to limit the amount of events sent, while keeping everything interleaved.
 */
public abstract class WebSocketRequest {

    /**
     * @return the type of the request.
     */
    public abstract String getRequestType();

    /**
     * @return the actual request name (say, subtype)
     */
    public abstract String getRequestName();

    public boolean canMergeWith(WebSocketRequest otherEvent) {
        return false;
    }

    public WebSocketRequest mergeWith(WebSocketRequest otherEvent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Specify the proto message that dictates how the request data will
     * be serialized. By default this returns null, meaning no request data
     * will be added.
     */
    public Message<?> getRequestData() {
        return null;
    }

    // Would prefer not to use rawtypes, but want to keep this class free of generics
    // to better support request that don't have any data. JsonIOUtil.writeTo should in my
    // opinion probably have used Message<T> instead of T for the message
    @SuppressWarnings({ "rawtypes", "unchecked" })
    WebSocketFrame toWebSocketFrame(String mediaType, int seqId) {
        // TODO support at least gpb as well
        //if (WSConstants.JSON_MIME_TYPE.equals(mediaType)) {

        Message<?> requestData = getRequestData();
        StringBuilder buf = new StringBuilder("[").append(WSConstants.PROTOCOL_VERSION)
                .append(",").append(WSConstants.MESSAGE_TYPE_REQUEST)
                .append(",").append(seqId)
                .append(",{\"")
                .append(getRequestType())
                .append("\":\"")
                .append(getRequestName())
                .append("\"");

        if (requestData != null) {
            ByteArrayOutputStream barray = new ByteArrayOutputStream();
            try {
                JsonIOUtil.writeTo(barray, (Message) requestData, (Schema) requestData.cachedSchema(), false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            buf.append(",\"data\":").append(barray.toString());
        }
        buf.append("}]");
        return new TextWebSocketFrame(buf.toString());
    }

    @Override
    public String toString() {
        return getRequestType() + "/" + getRequestName() + ": " + getRequestData();
    }
}
