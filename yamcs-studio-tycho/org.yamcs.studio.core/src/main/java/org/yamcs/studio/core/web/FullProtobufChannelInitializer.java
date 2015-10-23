package org.yamcs.studio.core.web;

import java.io.InputStream;

import org.yamcs.protobuf.Web.RestExceptionMessage;

import com.google.protobuf.MessageLite;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Assumes responses are smaller than 1MB and aggregates any http content before unserializing
 */
public class FullProtobufChannelInitializer extends ChannelInitializer<SocketChannel> {

    private MessageLite.Builder target;
    private ResponseHandler handler;

    public FullProtobufChannelInitializer(MessageLite.Builder target, ResponseHandler handler) {
        this.target = target;
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpClientCodec());
        p.addLast(new HttpObjectAggregator(1048576));
        p.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
            @Override
            public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
                if (HttpResponseStatus.OK.equals(response.getStatus())) {
                    MessageLite responseMsg = null;
                    if (target != null)
                        responseMsg = target.mergeFrom(new ByteBufInputStream(response.content())).build();
                    ctx.close();
                    handler.onMessage(responseMsg);
                } else {
                    InputStream in = new ByteBufInputStream(response.content());
                    RestExceptionMessage msg = RestExceptionMessage.newBuilder().mergeFrom(in).build();
                    ctx.close();
                    handler.onException(new RestException(msg));
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                cause.printStackTrace();
                ctx.close();
                if (cause instanceof Exception) {
                    handler.onException((Exception) cause);
                } else {
                    handler.onException(new Exception(cause));
                }
            }
        });
    }
}
