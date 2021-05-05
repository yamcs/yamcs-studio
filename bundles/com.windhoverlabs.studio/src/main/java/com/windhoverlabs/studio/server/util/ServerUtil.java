package com.windhoverlabs.studio.server.util;
	
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Collection of utilities to talk to YAMCS Server.
 * @author lgomez
 *
 */
public class ServerUtil {
	    private static JsonObject getRequest(String address) {
	    	
	    	JsonObject rootObject = null;
	        try {

	            URL url = new URL(address);
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("GET");
	            conn.connect();

	            //Getting the response code
	            int responsecode = conn.getResponseCode();

	            if (responsecode != 200) {
	                throw new RuntimeException("HttpResponseCode: " + responsecode);
	            } else {

	                String inline = "";
	                Scanner scanner = new Scanner(url.openStream());

	                //Write all the JSON data into a string using a scanner
	                while (scanner.hasNext()) {
	                    inline += scanner.nextLine();
	                }

	                //Close the scanner
	                scanner.close();

	                //Using the JSON simple library parse the string into a json object
	                JsonParser parser = new JsonParser();
	                JsonElement jsonElement = parser.parse(inline);
	                rootObject = jsonElement.getAsJsonObject();
	                
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
            return rootObject;
	    }

	
	public static void configureUpdTcDataLink(int port, String ipAddress) 
	{
		String route = String.format("http://localhost:8090/api/yamcs-cfs/studio/configureUpdTcDataLink/:%s,%d", ipAddress, port);
		getRequest(route);
	}

}
