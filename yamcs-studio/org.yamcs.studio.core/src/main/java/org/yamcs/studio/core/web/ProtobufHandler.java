package org.yamcs.studio.core.web;

import com.google.protobuf.MessageLite;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class ProtobufHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private ResponseHandler handler;

    public ProtobufHandler(ResponseHandler handler) {
        this.handler = handler;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        MessageLite proto = generator().newBuilder().mergeFrom(msg.array()).build();
        handler.onMessage(proto);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        handler.onException((Exception) cause);
    }

    public abstract BuilderGenerator generator();

    /**
     * Used to bypass protobuf restriction of only being able to make builders from an existing
     * message. Perhaps there's a better way.
     */
    public static interface BuilderGenerator {
        MessageLite.Builder newBuilder();
    }
}
