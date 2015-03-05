package org.csstudio.platform.libs.yamcs.web;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.protostuff.JsonIOUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.Message;
import io.protostuff.ProtobufIOUtil;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.csstudio.platform.libs.yamcs.YamcsConnectionProperties;
import org.yamcs.web.rest.protobuf.DumpRawMdbRequest;
import org.yamcs.web.rest.protobuf.DumpRawMdbResponse;
import org.yamcs.web.rest.protobuf.ListAvailableParametersRequest;
import org.yamcs.web.rest.protobuf.ListAvailableParametersResponse;
import org.yamcs.web.rest.protobuf.RESTService;
import org.yamcs.web.rest.protobuf.RequestArchiveRequest;
import org.yamcs.web.rest.protobuf.RequestArchiveResponse;
import org.yamcs.web.rest.protobuf.ValidateCommandRequest;
import org.yamcs.web.rest.protobuf.ValidateCommandResponse;

/**
 * Implements the client-side API of the rest web api.
 * Sequences outgoing requests on a single thread for simplicity.
 * 
 * Instances should be shutdown() when no longer in use.
 */
public class RESTClientEndpoint implements RESTService {
    
    private YamcsConnectionProperties yprops;
    private ExecutorService exec = Executors.newSingleThreadExecutor();
    
    // Reuse the same buffer for serializing multiple post requests
    private LinkedBuffer contentBuffer = LinkedBuffer.allocate(4096); // Increase value when requests are getting too big

    public RESTClientEndpoint(YamcsConnectionProperties yprops) {
        this.yprops = yprops;
    }
    
    @Override
    public void requestArchive(RequestArchiveRequest request, ResponseHandler<RequestArchiveResponse> responseHandler) {
        // TODO Auto-generated method stub
    }

    @Override
    public void validateCommand(ValidateCommandRequest request, ResponseHandler<ValidateCommandResponse> responseHandler) {
        URI uri = yprops.webResourceURI("/commanding/validate");
        doPOST(uri, request, new ValidateCommandResponse(), responseHandler);
    }

    @Override
    public void listAvailableParameters(ListAvailableParametersRequest request, ResponseHandler<ListAvailableParametersResponse> responseHandler) {
        URI uri = yprops.webResourceURI("/mdb/parameters");
        doGET(uri, new ListAvailableParametersResponse(), responseHandler);
    }
    
    @Override
    public void dumpRawMdb(DumpRawMdbRequest request, ResponseHandler<DumpRawMdbResponse> responseHandler) {
        URI uri = yprops.webResourceURI("/mdb/dump");
        doGET(uri, new DumpRawMdbResponse(), responseHandler);
    }
    
    private <T extends Message<T>> void doGET(URI uri, T target, ResponseHandler<T> handler) {
        exec.execute(() -> {
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
                                handler.onFault(cause);
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
        });
    }
    
    private <S extends Message<S>, T extends Message<T>> void doPOST(URI uri, S msg, T target, ResponseHandler<T> handler) {
        exec.execute(() -> {
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
                                handler.onFault(cause);
                            }
                        });
                    }
                });
    
                Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
                FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath());
                request.headers().set(HttpHeaders.Names.HOST, uri.getHost());
                request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
                request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");
                request.headers().set(HttpHeaders.Names.ACCEPT, "application/octet-stream");
                // Unclear why we need this contentbuffer. protobufioutil seems to first write there, and only afterwards stream out
                ProtobufIOUtil.writeTo(new ByteBufOutputStream(request.content()), msg, msg.cachedSchema(), contentBuffer);
                contentBuffer.clear(); // Prepare for next usage
                request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
                ch.writeAndFlush(request);
                ch.closeFuture().sync();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        });
    }
    
    @Override
    public void shutdown() {
        exec.shutdown();
    }
    
    public static void main(String... args) throws IOException {
        ValidateCommandRequest req = new ValidateCommandRequest();
        req.setCommandString("SWITCH_VOLTAGE_ON(votlage_num=4)");
        JsonIOUtil.writeTo(System.out, req, req.cachedSchema(), false);
        
        //
        YamcsConnectionProperties yprops = new YamcsConnectionProperties("localhost", 8080, "s3");
        RESTClientEndpoint client = new RESTClientEndpoint(yprops);
        client.validateCommand(req, new ResponseHandler<ValidateCommandResponse>() {
            @Override
            public void onMessage(ValidateCommandResponse msg) {
                System.out.println("got back msg "+msg);
                
            }

            @Override
            public void onFault(Throwable t) {
                System.out.println("got back fault "+t);
                t.printStackTrace();
            }
        });
        
    }
}
