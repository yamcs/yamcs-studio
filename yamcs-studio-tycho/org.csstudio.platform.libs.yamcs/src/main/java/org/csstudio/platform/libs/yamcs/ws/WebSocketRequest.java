package org.csstudio.platform.libs.yamcs.ws;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.protostuff.ByteString;
import io.protostuff.LinkedBuffer;
import io.protostuff.Message;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.yamcs.protostuff.WebSocketClientMessage;

/**
 * Tags anything to be sent upstream on an established websocket. Enables extending classes to
 * declare whether they can 'merge' with the 'next' event (events are queued while there is no
 * connection). This type of behaviour would allow the web-socket client to limit the amount of
 * events sent, while keeping everything interleaved.
 */
public abstract class WebSocketRequest {

    /**
     * @return the type of the resource.
     */
    public abstract String getResource();

    /**
     * @return the operation on the resource
     */
    public abstract String getOperation();

    public boolean canMergeWith(WebSocketRequest otherEvent) {
        return false;
    }

    public WebSocketRequest mergeWith(WebSocketRequest otherEvent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Specify the proto message that dictates how the request data will be serialized. By default
     * this returns null, meaning no request data will be added.
     */
    public Message<?> getRequestData() {
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    WebSocketFrame toWebSocketFrame(int seqId) {
        WebSocketClientMessage msg = new WebSocketClientMessage();
        msg.setProtocolVersion(WSConstants.PROTOCOL_VERSION);
        msg.setSequenceNumber(seqId);
        msg.setResource(getResource());
        msg.setOperation(getOperation());

        Message data = getRequestData();
        if (data != null) {
            try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
                // TODO research what this linkedbuffer is doing here. Can we use it?
                ProtobufIOUtil.writeTo(bout, data, (Schema<Message>) data.cachedSchema(), LinkedBuffer.allocate());
                // Surely there's a better way :-(
                msg.setData(ByteString.copyFrom(bout.toByteArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ByteBuf buf = Unpooled.buffer();
        try (ByteBufOutputStream bout = new ByteBufOutputStream(buf)) {
            ProtobufIOUtil.writeTo(bout, msg, msg.cachedSchema(), LinkedBuffer.allocate());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new BinaryWebSocketFrame(buf);
    }

    @Override
    public String toString() {
        return getResource() + "/" + getOperation() + ": " + getRequestData();
    }
}
