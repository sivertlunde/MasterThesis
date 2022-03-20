package pss.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataFetcher {
	
	public static JsonObject fetchDataFromUrl(String input) {
		
		JsonParser parser = new JsonParser();
		
		try {
			URL url = new URL(input);
			URLConnection request = url.openConnection();
			request.connect();
			InputStreamReader reader = new InputStreamReader((InputStream) request.getContent());
			JsonElement root = parser.parse(reader);
			return root.getAsJsonObject();

		} 
		catch (IOException e) {
			return null;
		}
		
	}
}
