package com.windhoverlabs.studio.server.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Scanner;

import org.yamcs.client.YamcsClient;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.logging.Logger;

/**
 * Collection of utilities to talk to YAMCS Server.
 * 
 * @author lgomez
 *
 */
public class ServerUtil {
    static final Logger log = Logger.getLogger(ServerUtil.class.getName());

    private static LinkedHashMap<Object, Object> request(String address, String method, String body) {
        LinkedHashMap<Object, Object> outMap = new LinkedHashMap<Object, Object>();
        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(address))
                    .method(method, HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "text/xml")
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            int responsecode = 200;

            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {

                String inline = response.body();

                // Using the JSON simple library parse the string into a json object
                Gson gson = new Gson();

                outMap = gson.fromJson(inline, LinkedHashMap.class);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return outMap;
    }

    public static void configureUpdTcDataLink(int linkPort, String ipAddress, String linkName) {
        String activeInstance = YamcsPlugin.getInstance();
        System.out.println("All fields for server:" + YamcsPlugin.getProcessorInfo().getAllFields());
        if (activeInstance != null) {
            String route = String.format("http://localhost:8090/api/UpdTcDataLink/%s/%s/%s/%d:configure",
                    activeInstance, linkName, ipAddress, linkPort);
            // TODO Add body to method call
            request(route, "PATCH", "");
        } else {
            System.out.println("Not active instance at the moment.");
        }
    }

    /**
     * Request Sch Table entries specific to an app from the server.
     * 
     * @param paramPath
     *            The XTCE parameter path such as "/cfs//sch/SCH_DiagPacket_t" that the entries will be extracted from.
     * @param processorName
     * @param appName
     * @return A map in the format of "{schEntries: [{minor=225, activityNumber=11, messageMacro=SC_1HZ_WAKEUP_MID,
     *         state=ENABLED}, {minor=230, activityNumber=11, messageMacro=SC_SEND_HK_MID, state=ENABLED}]}"
     * 
     */
    public static LinkedHashMap<Object, Object> getSchTableForApp(String paramPath,
            String appName) {
        String activeInstance = YamcsPlugin.getInstance();
        LinkedHashMap<Object, Object> schTable = null;
        if (activeInstance != null) {
            int port = YamcsPlugin.getYamcsClient().getPort();
            String host = YamcsPlugin.getYamcsClient().getHost();

            schTable = new LinkedHashMap<Object, Object>();

            // TODO In the future yamcs-cfs should have a client API that wraps around the http calls
            String route = String.format("http://%s:%d/api/%s/cfs/sch/table", host, port, activeInstance);
            System.out.println("processor:" + YamcsPlugin.getProcessor());
            schTable = request(route, "GET", String.format(
                    "{'paramPath':'%s', 'processor': '%s','app': '%s'}", paramPath, YamcsPlugin.getProcessor(),
                    appName));

        } else {
            log.warning("No active instance at the moment.");
        }

        return schTable;
    }

    /**
     * Request counters such as fatFrameCount and rcvdCaduCount to zero.
     * 
     * 
     * @param linkName
     * @return A map with the request's response.
     * 
     */
    public static LinkedHashMap<Object, Object> resetSdlpPacketInputStreamCounters(String linkName) {
        String activeInstance = YamcsPlugin.getInstance();
        LinkedHashMap<Object, Object> response = null;
        if (activeInstance != null) {
            int port = YamcsPlugin.getYamcsClient().getPort();
            String host = YamcsPlugin.getYamcsClient().getHost();

            response = new LinkedHashMap<Object, Object>();

            // TODO In the future yamcs-cfs should have a client API that wraps around the http calls
            String route = String.format("http://%s:%d/api/%s/links/%s/streams/SdlpPacketInputStream:reset", host, port,
                    activeInstance, linkName);
            response = request(route, "GET", "{}");

        } else {
            log.warning("No active instance at the moment.");
        }

        return response;
    }
    
    /**
     * Sets the expected length of a frame.
     * 
     * @param linkName
     * @return A map with the request's response.
     * 
     */
    public static LinkedHashMap<Object, Object> setFixedLength(String linkName) {
        String activeInstance = YamcsPlugin.getInstance();
        LinkedHashMap<Object, Object> response = null;
        if (activeInstance != null) {
            int port = YamcsPlugin.getYamcsClient().getPort();
            String host = YamcsPlugin.getYamcsClient().getHost();

            response = new LinkedHashMap<Object, Object>();

            // TODO In the future yamcs-cfs should have a client API that wraps around the http calls
            String route = String.format("http://%s:%d/api/%s/links/%s/streams/SdlpPacketInputStream:reconfigure", host, port,
                    activeInstance, linkName);
            response = request(route, "GET", "{}");

        } else {
            log.warning("No active instance at the moment.");
        }

        return response;
    }
}
