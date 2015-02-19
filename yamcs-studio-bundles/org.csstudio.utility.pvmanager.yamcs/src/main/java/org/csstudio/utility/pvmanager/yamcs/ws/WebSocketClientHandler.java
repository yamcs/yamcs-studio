package org.csstudio.utility.pvmanager.yamcs.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import io.protostuff.JsonIOUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.yamcs.protobuf.NamedObjectId;
import org.yamcs.protobuf.NamedObjectList;
import org.yamcs.protobuf.ParameterData;
import org.yamcs.protobuf.ParameterValue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    
    private JsonFactory jsonFactory = new JsonFactory();

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
        System.out.println("WebSocket Client disconnected!");
        client.setConnected(false);
        callback.onDisconnect();
        if (client.isReconnectionEnabled()) {
            // TODO this is actually not enough. we would also need
            // to resubscribe (probably only after checking if really we have
            // lost our subscription)
            ctx.channel().eventLoop().schedule(() -> client.connect(), 1L, TimeUnit.SECONDS);
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            System.out.println("WebSocket Client connected!!");
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
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            System.out.println("WebSocket Client received message: " + textFrame.text());
            decodeMessage(textFrame);
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }
    
    private void decodeMessage(TextWebSocketFrame textFrame) {
        try {
            String jstext = textFrame.text();
            JsonParser jsp=jsonFactory.createParser(jstext);
            if(jsp.nextToken()!=JsonToken.START_ARRAY) throw new RuntimeException("Invalid message (expecting an array)");
            if(jsp.nextToken()!=JsonToken.VALUE_NUMBER_INT) throw new RuntimeException("Invalid message (expecting version as an integer number)");
            int version=jsp.getIntValue();
            if(version!=WSConstants.PROTOCOL_VERSION) throw new RuntimeException("Invalid version (expecting "+WSConstants.PROTOCOL_VERSION+" received "+version);
    
            if(jsp.nextToken()!=JsonToken.VALUE_NUMBER_INT) throw new RuntimeException("Invalid message (expecting type as an integer number)");
            int messageType=jsp.getIntValue();
            
            if(jsp.nextToken()!=JsonToken.VALUE_NUMBER_INT) throw new RuntimeException("Invalid message (expecting seqId as an integer number)");
            int seqId=jsp.getIntValue();
            
            if (messageType == WSConstants.MESSAGE_TYPE_DATA) {
                decodeDataMessage(seqId, jsp);
            } else if (messageType == WSConstants.MESSAGE_TYPE_REPLY) {
                client.ackSubscription(seqId);
            } else if (messageType == WSConstants.MESSAGE_TYPE_EXCEPTION) {
                decodeExceptionMessage(seqId, jsp);
            } else {
                throw new RuntimeException("Invalid message type received: "+messageType);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void decodeDataMessage(int seqId, JsonParser jsp) throws JsonParseException, IOException {
        if(jsp.nextToken()!=JsonToken.START_OBJECT) throw new RuntimeException("Invalid message (expecting an object)");
        if ((jsp.nextToken()!=JsonToken.FIELD_NAME) || (!"dt".equals(jsp.getCurrentName()))) throw new RuntimeException("Invalid message (expecting dt as the first field)");
        if (jsp.nextToken()!=JsonToken.VALUE_STRING) throw new RuntimeException("Invalid message (expecting value for dt as a string)");
        String dtype=jsp.getText();
        
        if((jsp.nextToken()!=JsonToken.FIELD_NAME) || (!"data".equals(jsp.getCurrentName())))
            throw new RuntimeException("Invalid message (expecting data as the next field)");
       
        if (dtype.equals("ParameterData")) {
            decodeParameterDataMessage(seqId, jsp);
        } else {
            throw new RuntimeException("Unsupported dt-value "+dtype);
        }
    }
    
    private void decodeParameterDataMessage(long seqId, JsonParser jsdata) throws IOException {
        ParameterData pdata = new ParameterData();
        JsonIOUtil.mergeFrom(jsdata, pdata, pdata.cachedSchema(), false);
        for (ParameterValue pval : pdata.getParameterList()) {
            System.out.println("  - " + pval.getId().getName());
        }
        callback.onParameterData(pdata);
    }

    private void decodeExceptionMessage(int seqId, JsonParser jsp) throws IOException {
        if(jsp.nextToken()!=JsonToken.START_OBJECT) throw new RuntimeException("Invalid message (expecting an object)");
        if ((jsp.nextToken()!=JsonToken.FIELD_NAME) || (!"et".equals(jsp.getCurrentName()))) throw new RuntimeException("Invalid message (expecting et as the first field)");
        if (jsp.nextToken()!=JsonToken.VALUE_STRING) throw new RuntimeException("Invalid message (expecting value for et as a string)");
        String etype=jsp.getText();
        
        if((jsp.nextToken()!=JsonToken.FIELD_NAME) || (!"msg".equals(jsp.getCurrentName())))
            throw new RuntimeException("Invalid message (expecting msg as the next field)");
       
        if (etype.equals("InvalidIdentification")) {
            // Well that's unfortunate, we need to resend another subscription with
            // the invalid parameters excluded
            NamedObjectList invalidList = new NamedObjectList();
            JsonIOUtil.mergeFrom(jsp, invalidList, invalidList.cachedSchema(), false);
            
            NamedObjectList list = client.getUpstreamSubscription(seqId);
            
            // TODO protostuff doesn't automatically generate equals/hashcode :-(
            for (NamedObjectId invalidId : invalidList.getListList()) {
                // Notify downstream channels
                callback.onInvalidIdentification(invalidId);
                
                for (NamedObjectId id : list.getListList()) {
                    if (id.getName().equals(invalidId.getName()) && id.getNamespace().equals(invalidId.getNamespace())) {
                        list.getListList().remove(id);
                        System.out.println("Removed "+invalidId.getName());
                        break;
                    }
                }
            }
            
            // Get rid of the old subscription
            client.ackSubscription(seqId);
            
            // And have another go at it
            client.subscribe(list);
            
        } else {
            System.out.println("Got exception message " + etype);
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
