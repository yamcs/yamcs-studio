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
import org.yamcs.protostuff.RESTService;
import org.yamcs.protostuff.ReplayRequest;
import org.yamcs.protostuff.RestDumpRawMdbRequest;
import org.yamcs.protostuff.RestDumpRawMdbResponse;
import org.yamcs.protostuff.RestListAvailableParametersRequest;
import org.yamcs.protostuff.RestListAvailableParametersResponse;
import org.yamcs.protostuff.RestReplayResponse;
import org.yamcs.protostuff.RestSendCommandRequest;
import org.yamcs.protostuff.RestSendCommandResponse;
import org.yamcs.protostuff.RestValidateCommandRequest;
import org.yamcs.protostuff.RestValidateCommandResponse;

/**
 * Implements the client-side API of the rest web api.
 * Sequences outgoing requests on a single thread for simplicity.
 * 
 * Instances should be shutdown() when no longer in use.
 * 
 * TODO accepts only json for now, because exception are not currently
 * sent in gpb format.
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
    public void replay(ReplayRequest request, ResponseHandler<RestReplayResponse> responseHandler) {
        // TODO Auto-generated method stub
    }

    @Override
    public void validateCommand(RestValidateCommandRequest request, ResponseHandler<RestValidateCommandResponse> responseHandler) {
        URI uri = yprops.webResourceURI("/api/commanding/validate");
        doPOST(uri, request, new RestValidateCommandResponse(), responseHandler);
    }
    
    @Override
    public void sendCommand(RestSendCommandRequest request, ResponseHandler<RestSendCommandResponse> responseHandler) {
        URI uri = yprops.webResourceURI("/api/commanding/send");
        doPOST(uri, request, new RestSendCommandResponse(), responseHandler);
    }

    @Override
    public void listAvailableParameters(RestListAvailableParametersRequest request, ResponseHandler<RestListAvailableParametersResponse> responseHandler) {
        URI uri = yprops.webResourceURI("/api/mdb/parameters");
        doGET(uri, new RestListAvailableParametersResponse(), responseHandler);
    }
    
    @Override
    public void dumpRawMdb(RestDumpRawMdbRequest request, ResponseHandler<RestDumpRawMdbResponse> responseHandler) {
        URI uri = yprops.webResourceURI("/api/mdb/dump");
        doGET(uri, new RestDumpRawMdbResponse(), responseHandler);
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
                                //ProtobufIOUtil.mergeFrom(new ByteBufInputStream(response.content()), target, target.cachedSchema());
                                JsonIOUtil.mergeFrom(new ByteBufInputStream(response.content()), target, target.cachedSchema(), false);
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
                request.headers().set(HttpHeaders.Names.ACCEPT, "application/json");
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
                                JsonIOUtil.mergeFrom(new ByteBufInputStream(response.content()), target, target.cachedSchema(), false);
                                //ProtobufIOUtil.mergeFrom(new ByteBufInputStream(response.content()), target, target.cachedSchema());
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
                request.headers().set(HttpHeaders.Names.ACCEPT, "application/json");
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
        RestValidateCommandRequest req = new RestValidateCommandRequest();
        //CommandParser.toCommand("SWITCH_VOLTAGE_ON(votlage_num=4)");
        //req.setCommandString("SWITCH_VOLTAGE_ON(votlage_num=4)");
        JsonIOUtil.writeTo(System.out, req, req.cachedSchema(), false);
        
        //
        YamcsConnectionProperties yprops = new YamcsConnectionProperties("localhost", 8090, "s3");
        RESTClientEndpoint client = new RESTClientEndpoint(yprops);
        client.validateCommand(req, new ResponseHandler<RestValidateCommandResponse>() {
            @Override
            public void onMessage(RestValidateCommandResponse msg) {
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
