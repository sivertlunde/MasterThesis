package com.pss.util;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pss.model.CveItem;

public class CustomJsonParser {
	
	public static List<CveItem> parseCveItems(String artifactId, JsonObject nvdResponse) {
		List<CveItem> items = new ArrayList<>();
		JsonArray cveItems = nvdResponse.get("result").getAsJsonObject().get("CVE_Items").getAsJsonArray();
		if (!cveItems.isEmpty()) {
			for (JsonElement je : cveItems) {
				JsonObject jo = je.getAsJsonObject();
				String cveId = jo.get("cve").getAsJsonObject().get("CVE_data_meta").getAsJsonObject().get("ID").getAsString();
				items.add(new CveItem(artifactId, cveId, jo));
			}
		}
		return items;
	}

}
