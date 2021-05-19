package com.windhoverlabs.studio.server.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Scanner;

import org.yamcs.client.YamcsClient;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * Collection of utilities to talk to YAMCS Server.
 * 
 * @author lgomez
 *
 */
public class ServerUtil {
    private static JsonObject request(String address, String method, String body) {

        JsonObject rootObject = null;
        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(address))
                    .method(method, HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "text/xml")

                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            System.out.println("reposne: %s" + response.body());

            //
            // client.sendAsync(request, BodyHandlers.ofString())
            // .thenApply(HttpResponse::body)
            // .thenAccept(System.out::println)
            // .join();

            // request.
            // Getting the response code
            int responsecode = 200;

            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {

                String inline = "{}";
                // Scanner scanner = new Scanner(url.openStream());
                //
                // //Write all the JSON data into a string using a scanner
                // while (scanner.hasNext()) {
                // inline += scanner.nextLine();
                // }
                //
                // //Close the scanner
                // scanner.close();

                // Using the JSON simple library parse the string into a json object
                JsonParser parser = new JsonParser();
                JsonElement jsonElement = parser.parse(inline);
                rootObject = jsonElement.getAsJsonObject();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootObject;
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
     * response = requests.get(f'http://localhost:8090/api/{INSTANCE}/cfs/sch/table', json={'paramPath':
     * '/cfs//sch/SCH_DiagPacket_t', 'processor': 'realtime','app': 'none'})
     * 
     * @param paramPath
     * @param processorName
     * @param appName
     * @return
     */

    public static LinkedHashMap<Object, Object> getSchTableForApp(String paramPath, String processorName,
            String appName) {
        String activeInstance = YamcsPlugin.getInstance();

        int port = YamcsPlugin.getYamcsClient().getPort();
        String host = YamcsPlugin.getYamcsClient().getHost();

        LinkedHashMap<Object, Object> schTable = new LinkedHashMap<Object, Object>();

        if (activeInstance != null) {
            String route = String.format("http://%s:%d/api/%s/cfs/sch/table", host, port,
                    activeInstance);
            request(route, "GET", "{'paramPath':'/cfs//sch/SCH_DiagPacket_t', 'processor': 'realtime','app': 'ds'}");

        }

        return schTable;
    }
}
