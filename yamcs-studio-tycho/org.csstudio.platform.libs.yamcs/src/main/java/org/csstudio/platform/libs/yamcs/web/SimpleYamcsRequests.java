package org.csstudio.platform.libs.yamcs.web;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.protostuff.Message;
import io.protostuff.ProtobufIOUtil;

import java.net.URI;

import org.csstudio.platform.libs.yamcs.YamcsConnectionProperties;
import org.yamcs.protobuf.NamedObjectList;

public class SimpleYamcsRequests {
    
    public static void listAllAvailableParameters(YamcsConnectionProperties yprops, MessageHandler<NamedObjectList> handler) {
        URI uri = yprops.webResourceURI("/mdb/parameters");
        doSimpleRequest(uri, new NamedObjectList(), handler);
    }
    
    private static <T extends Message<T>> void doSimpleRequest(URI uri, T target, MessageHandler<T> handler) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new HttpClientCodec());
                    p.addLast(new HttpObjectAggregator(1048576));
                    p.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                        @Override
                        public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
                            ProtobufIOUtil.mergeFrom(new ByteBufInputStream(response.content()), target, target.cachedSchema());
                            ctx.close();
                            handler.onMessage(target);
                        }
                        
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            cause.printStackTrace();
                            ctx.close();
                            handler.onException(cause);
                        }
                    });
                }
            });

            Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
            request.headers().set(HttpHeaders.Names.HOST, uri.getHost());
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(HttpHeaders.Names.ACCEPT, "application/octet-stream");
            ch.writeAndFlush(request);
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
