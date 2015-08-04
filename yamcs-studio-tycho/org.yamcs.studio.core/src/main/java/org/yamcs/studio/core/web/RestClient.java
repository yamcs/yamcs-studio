package org.yamcs.studio.core.web;

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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Rest.RestDumpArchiveRequest;
import org.yamcs.protobuf.Rest.RestDumpArchiveResponse;
import org.yamcs.protobuf.Rest.RestDumpRawMdbRequest;
import org.yamcs.protobuf.Rest.RestDumpRawMdbResponse;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.Rest.RestListAuthorisationsRequest;
import org.yamcs.protobuf.Rest.RestListAuthorisationsResponse;
import org.yamcs.protobuf.Rest.RestListAvailableParametersRequest;
import org.yamcs.protobuf.Rest.RestListAvailableParametersResponse;
import org.yamcs.protobuf.Rest.RestListProcessorsRequest;
import org.yamcs.protobuf.Rest.RestListProcessorsResponse;
import org.yamcs.protobuf.Rest.RestSendCommandRequest;
import org.yamcs.protobuf.Rest.RestSendCommandResponse;
import org.yamcs.protobuf.Rest.RestValidateCommandRequest;
import org.yamcs.protobuf.Rest.RestValidateCommandResponse;
import org.yamcs.protobuf.Yamcs.CommandHistoryReplayRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest;
import org.yamcs.studio.core.YamcsCredentials;

import com.google.protobuf.MessageLite;

/**
 * Implements the client-side API of the rest web api. Sequences outgoing requests on a single
 * thread for simplicity.
 */
public class RestClient {

    private static final Logger log = Logger.getLogger(RestClient.class.getName());
    private static final String BINARY_MIME_TYPE = "application/octet-stream";

    private YamcsConnectionProperties yprops;
    private EventLoopGroup group = new NioEventLoopGroup(1);

    private YamcsCredentials credentials;

    public RestClient(YamcsConnectionProperties yprops, YamcsCredentials yamcsCredentials) {
        this.yprops = yprops;
        this.credentials = yamcsCredentials;

    }

    public void dumpArchive(RestDumpArchiveRequest request, ResponseHandler responseHandler) {
        get("/api/archive", request, RestDumpArchiveResponse.newBuilder(), responseHandler);
    }

    public void validateCommand(RestValidateCommandRequest request, ResponseHandler responseHandler) {
        post("/api/commanding/validator", request, RestValidateCommandResponse.newBuilder(), responseHandler);
    }

    public void sendCommand(RestSendCommandRequest request, ResponseHandler responseHandler) {
        post("/api/commanding/queue", request, RestSendCommandResponse.newBuilder(), responseHandler);
    }

    public void listAvailableParameters(RestListAvailableParametersRequest request, ResponseHandler responseHandler) {
        get("/api/mdb/parameters", request, RestListAvailableParametersResponse.newBuilder(), responseHandler);
    }

    public void dumpRawMdb(RestDumpRawMdbRequest request, ResponseHandler responseHandler) {
        get("/api/mdb/dump", null, RestDumpRawMdbResponse.newBuilder(), responseHandler);
    }

    public void setParameters(ParameterData request, ResponseHandler responseHandler) {
        post("/api/parameter/_set", request, null, responseHandler);
    }

    public void createProcessorManagementRequest(ProcessorManagementRequest request, ResponseHandler responseHandler) {
        post("/api/processor", request, null, responseHandler);
    }

    public void createProcessorRequest(String processorName, ProcessorRequest request, ResponseHandler responseHandler) {
        post("/api/processor/" + processorName, request, null, responseHandler);
    }

    public void listProcessors(RestListProcessorsRequest request, ResponseHandler responseHandler) {
        get("/api/processor/list", request, RestListProcessorsResponse.newBuilder(), responseHandler);
    }

    public void listAuthorizations(RestListAuthorisationsRequest request, ResponseHandler responseHandler) {
        get("/api/authorization/list", request, RestListAuthorisationsResponse.newBuilder(), responseHandler);
    }

    public void get(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        doRequest(HttpMethod.GET, uri, msg, target, handler);
    }

    public void post(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        doRequest(HttpMethod.POST, uri, msg, target, handler);
    }

    private <S extends MessageLite> void doRequest(HttpMethod method, String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        URI resource = yprops.webResourceURI(uri);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
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
                    });

            Channel ch = b.connect(resource.getHost(), resource.getPort()).sync().channel();
            FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, resource.getRawPath());
            request.headers().set(HttpHeaders.Names.HOST, resource.getHost());
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(HttpHeaders.Names.ACCEPT, BINARY_MIME_TYPE);

            if (credentials != null) {
                String credentialsClear = credentials.getUsername();
                if (credentials.getPasswordS() != null)
                    credentialsClear += ":" + credentials.getPasswordS();
                String credentialsB64 = new String(Base64.getEncoder().encode(credentialsClear.getBytes()));
                String authorization = "Basic " + credentialsB64;
                request.headers().set(HttpHeaders.Names.AUTHORIZATION, authorization);
            }

            if (msg != null) {
                msg.writeTo(new ByteBufOutputStream(request.content()));
                request.headers().set(HttpHeaders.Names.CONTENT_TYPE, BINARY_MIME_TYPE);
                request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
            }
            ch.writeAndFlush(request);
        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, "Could not execute REST call", e);
            handler.onException(e);
        }
    }

    /**
     * Performs an orderly shutdown of this service
     */
    public void shutdown() {
        group.shutdownGracefully();
    }

    public static void main(String... args) {
        RestDumpArchiveRequest req = RestDumpArchiveRequest.newBuilder().setCommandHistoryRequest(CommandHistoryReplayRequest.newBuilder()).build();
        RestClient endpoint = new RestClient(new YamcsConnectionProperties("machine", 8090, "simulator"),
                new YamcsCredentials("operator", "password"));
        System.out.println("ahum ");
        endpoint.dumpArchive(req, new ResponseHandler() {

            @Override
            public void onMessage(MessageLite responseMsg) {
                System.out.println("msg " + responseMsg);
            }

            @Override
            public void onException(Exception e) {
                System.out.println("e " + e);
            }
        });
        endpoint.shutdown();
    }
}
