package org.yamcs.studio.core.web;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Archive.DumpArchiveRequest;
import org.yamcs.protobuf.Archive.DumpArchiveResponse;
import org.yamcs.protobuf.Archive.GetTagsRequest;
import org.yamcs.protobuf.Archive.GetTagsResponse;
import org.yamcs.protobuf.Archive.InsertTagRequest;
import org.yamcs.protobuf.Archive.InsertTagResponse;
import org.yamcs.protobuf.Archive.UpdateTagRequest;
import org.yamcs.protobuf.Events.GetEventsRequest;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Rest.IssueCommandRequest;
import org.yamcs.protobuf.Rest.ListCommandsResponse;
import org.yamcs.protobuf.Rest.ListParametersResponse;
import org.yamcs.protobuf.Rest.ListProcessorsResponse;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.UserInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorManagementRequest;
import org.yamcs.protobuf.YamcsManagement.ProcessorRequest;
import org.yamcs.studio.core.security.YamcsCredentials;
import org.yamcs.studio.core.web.ProtobufHandler.BuilderGenerator;

import com.google.protobuf.MessageLite;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;

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

    public void dumpArchive(DumpArchiveRequest request, ResponseHandler responseHandler) {
        get("/archive", request, DumpArchiveResponse.newBuilder(), responseHandler);
    }

    public void sendCommand(String commandName, IssueCommandRequest request, ResponseHandler responseHandler) {
        post("/commands" + commandName, request, null, responseHandler);
    }

    public void listParameters(ResponseHandler responseHandler) {
        get("/parameters", null, ListParametersResponse.newBuilder(), responseHandler);
    }

    public void getParameter(String qualifiedName, ResponseHandler responseHandler) {
        get("/parameters" + qualifiedName, null, ParameterInfo.newBuilder(), responseHandler);
    }

    public void getEvents(GetEventsRequest request, ResponseHandler responseHandler) {
        streamGet("/events", request, () -> Event.newBuilder(), responseHandler);
    }

    public void listCommands(ResponseHandler responseHandler) {
        get("/commands", null, ListCommandsResponse.newBuilder(), responseHandler);
    }

    public void setParameters(ParameterData request, ResponseHandler responseHandler) {
        post("/parameter/_set", request, null, responseHandler);
    }

    public void createProcessorManagementRequest(ProcessorManagementRequest request, ResponseHandler responseHandler) {
        post("/processors", request, null, responseHandler);
    }

    public void createProcessorRequest(String processorName, ProcessorRequest request, ResponseHandler responseHandler) {
        post("/processors/" + processorName, request, null, responseHandler);
    }

    public void listProcessors(ResponseHandler responseHandler) {
        get("/processors", null, ListProcessorsResponse.newBuilder(), responseHandler);
    }

    public void getAuthenticatedUser(ResponseHandler responseHandler) {
        get("/user", null, UserInfo.newBuilder(), responseHandler);
    }

    public void insertTag(InsertTagRequest request, ResponseHandler responseHandler) {
        post("/archive/tags", request, InsertTagResponse.newBuilder(), responseHandler);
    }

    public void updateTag(long tagTime, int tagId, UpdateTagRequest request, ResponseHandler responseHandler) {
        put("/archive/tags/" + tagTime + "/" + tagId, request, null, responseHandler);
    }

    public void deleteTag(long tagTime, int tagId, ResponseHandler responseHandler) {
        delete("/archive/tags/" + tagTime + "/" + tagId, null, null, responseHandler);
    }

    public void getTags(GetTagsRequest request, ResponseHandler responseHandler) {
        get("/archive/tags", request, GetTagsResponse.newBuilder(), responseHandler);
    }

    public void get(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        doRequest(HttpMethod.GET, uri, msg, target, handler);
    }

    public void streamGet(String uri, MessageLite msg, BuilderGenerator generator, ResponseHandler handler) {
        doRequestWithDelimitedResponse(HttpMethod.GET, uri, msg, generator, handler);
    }

    public void post(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        doRequest(HttpMethod.POST, uri, msg, target, handler);
    }

    public void put(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        doRequest(HttpMethod.PUT, uri, msg, target, handler);
    }

    public void delete(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        doRequest(HttpMethod.DELETE, uri, msg, target, handler);
    }

    private <S extends MessageLite> void doRequest(HttpMethod method, String uri, MessageLite requestBody, MessageLite.Builder target, ResponseHandler handler) {
        URI resource = yprops.webResourceURI(uri);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new FullProtobufChannelInitializer(target, handler));
            initializeChannel(b, resource, method, requestBody);

        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, "Could not execute REST call", e);
            handler.onException(e);
        }
    }

    private <S extends MessageLite> void doRequestWithDelimitedResponse(HttpMethod method, String uri, MessageLite requestBody, BuilderGenerator builderGenerator, ResponseHandler handler) {
        // Currently doesn't correctly interpret RestExceptionMessage. Should probably
        // add a pipeline handler for HttpResponse for that
        URI resource = yprops.webResourceURI(uri);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new HttpClientCodec());
                    p.addLast(new HttpContentToByteBufDecoder());
                    p.addLast(new ProtobufVarint32FrameDecoder());
                    p.addLast(new ProtobufHandler(handler) {
                        @Override
                        public BuilderGenerator generator() {
                            return builderGenerator;
                        }
                    });
                }
            });
            // A very verbose way of signaling the end of the stream...
            // (UI components that bulk up data rely on this)
            ChannelFuture channelFuture = initializeChannel(b, resource, method, requestBody);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        future.channel().closeFuture().addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                handler.onMessage(null);
                            }
                        });
                    }
                }
            });
        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, "Could not execute REST call", e);
            handler.onException(e);
        }
    }

    private ChannelFuture initializeChannel(Bootstrap b, URI resource, HttpMethod method, MessageLite requestBody) throws IOException, InterruptedException {
        // FIXME no sync on call thread
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

        if (requestBody != null) {
            requestBody.writeTo(new ByteBufOutputStream(request.content()));
            request.headers().set(HttpHeaders.Names.CONTENT_TYPE, BINARY_MIME_TYPE);
            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
        }
        return ch.writeAndFlush(request);
    }

    /**
     * Performs an orderly shutdown of this service
     */
    public void shutdown() {
        group.shutdownGracefully();
    }

    public static void main(String... args) throws InterruptedException {
        //Events.GetEventsRequest req = RestDumpArchiveRequest.newBuilder().setCommandHistoryRequest(CommandHistoryReplayRequest.newBuilder()).build();
        RestClient endpoint = new RestClient(new YamcsConnectionProperties("machine", 8090, "simulator"),
                new YamcsCredentials("operator", "password"));
        System.out.println("ahum ");
        endpoint.getEvents(null, new ResponseHandler() {

            @Override
            public void onMessage(MessageLite responseMsg) {
                System.out.println("msg " + responseMsg);
            }

            @Override
            public void onException(Exception e) {
                System.out.println("e " + e);
            }
        });
        Thread.sleep(10000);
        endpoint.shutdown();
    }
}
