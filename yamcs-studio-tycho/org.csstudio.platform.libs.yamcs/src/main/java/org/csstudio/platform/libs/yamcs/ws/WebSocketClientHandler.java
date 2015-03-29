package org.csstudio.platform.libs.yamcs.ws;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import io.protostuff.ProtobufIOUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.protostuff.NamedObjectId;
import org.yamcs.protostuff.NamedObjectList;
import org.yamcs.protostuff.WebSocketServerMessage;
import org.yamcs.protostuff.WebSocketServerMessage.WebSocketExceptionData;
import org.yamcs.protostuff.WebSocketServerMessage.WebSocketSubscriptionData;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger log = Logger.getLogger(WebSocketClientHandler.class.getName());

    private final WebSocketClientHandshaker handshaker;
    private final WebSocketClient client;
    private final WebSocketClientCallbackListener callback;
    private ChannelPromise handshakeFuture;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, WebSocketClient client, WebSocketClientCallbackListener callback) {
        this.handshaker = handshaker;
        this.client = client;
        this.callback = callback;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("WebSocket Client disconnected!");
        client.setConnected(false);
        callback.onDisconnect();
        if (client.isReconnectionEnabled()) {
            // TODO this is actually not enough. we would also need to resubscribe
            ctx.channel().eventLoop().schedule(() -> client.connect(), 1L, TimeUnit.SECONDS);
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            log.info("WebSocket Client connected!!");
            client.setConnected(true);
            handshakeFuture.setSuccess();
            callback.onConnect();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus="
                            + response.getStatus() + ", content="
                            + response.content().toString(CharsetUtil.UTF_8)
                            + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
            if (log.isLoggable(Level.FINE)) {
                log.fine("WebSocket Client received message of size " + binaryFrame.content().readableBytes());
            }
            processFrame(binaryFrame);
        } else if (frame instanceof PongWebSocketFrame) {
            log.info("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            log.info("WebSocket Client received closing");
            ch.close();
        } else {
            log.severe("Received unsupported web socket frame " + frame);
            System.out.println(((TextWebSocketFrame) frame).text());
        }
    }

    private void processFrame(BinaryWebSocketFrame frame) {
        try {
            WebSocketServerMessage message = new WebSocketServerMessage();
            ProtobufIOUtil.mergeFrom(new ByteBufInputStream(frame.content()), message, message.cachedSchema());
            switch (message.getType()) {
            case REPLY:
                client.ackSubscription(message.getReply().getSequenceNumber());
                break;
            case EXCEPTION:
                processExceptionData(message.getException());
                break;
            case DATA:
                processSubscriptionData(message.getData());
                break;
            default:
                throw new IllegalStateException("Invalid message type received: " + message.getType());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processSubscriptionData(WebSocketSubscriptionData data) throws IOException {
        switch (data.getType()) {
        case PARAMETER:
            callback.onParameterData(data.getParameterData());
            break;
        case CMD_HISTORY:
            callback.onCommandHistoryData(data.getCommand());
            break;
        default:
            throw new IllegalStateException("Unsupported data type " + data.getType());
        }
    }

    private void processExceptionData(WebSocketExceptionData exceptionData) throws IOException {
        if ("InvalidIdentification".equals(exceptionData.getType())) {
            // Well that's unfortunate, we need to resend another subscription with
            // the invalid parameters excluded

            NamedObjectList invalidList = new NamedObjectList();
            byte[] barray = exceptionData.getData().toByteArray();
            ProtobufIOUtil.mergeFrom(barray, invalidList, invalidList.cachedSchema());

            NamedObjectList list = client.getUpstreamSubscription(exceptionData.getSequenceNumber());

            for (NamedObjectId invalidId : invalidList.getListList()) {
                // Notify downstream channels
                callback.onInvalidIdentification(invalidId);

                for (NamedObjectId id : list.getListList()) {
                    if (id.equals(invalidId)) {
                        list.getListList().remove(id);
                        log.info("Removed " + invalidId.getName());
                        break;
                    }
                }
            }

            // Get rid of the current pending request
            client.ackSubscription(exceptionData.getSequenceNumber());

            // And have another go at it
            client.sendRequest(new ParameterSubscribeRequest(list));
        } else {
            // TODO we should throw this up based on seqNr.
            log.severe("Got exception message " + exceptionData.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();

        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
