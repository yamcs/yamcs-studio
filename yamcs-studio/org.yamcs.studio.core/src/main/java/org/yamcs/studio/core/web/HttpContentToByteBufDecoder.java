package org.yamcs.studio.core.web;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpContent;

public class HttpContentToByteBufDecoder extends MessageToMessageDecoder<HttpContent> {

    @Override
    public void decode(ChannelHandlerContext ctx, HttpContent message, List<Object> out) throws Exception {
        out.add(message.content().retain());
    }
}
