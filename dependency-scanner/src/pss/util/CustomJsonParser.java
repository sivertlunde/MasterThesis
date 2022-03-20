package pss.util;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pss.model.CpeMatch;
import pss.model.CveItem;

public class CustomJsonParser {
	
	/**
	 * Iterates through potentially several NVD-entries returned in the 
	 * JsonResponse and returns the information parsed into CveItem objects.
	 * @param A JsonObject containing the entire response from NVD
	 * @return A list containing all CveItems, if any
	 */
	public static List<CveItem> parseCveItems(JsonObject nvdResponse) {
		List<CveItem> items = new ArrayList<>();
		JsonArray cveItems = nvdResponse.get("result").getAsJsonObject().get("CVE_Items").getAsJsonArray();
		if (cveItems.size() > 0) {
			for (JsonElement je : cveItems) {
				items.add(parseItem(je));
			}
		}
		return items;
	}
	
	
	/**
	 * Ugly parser - has to be done.
	 * @param JsonElement containing all information on an NVD entry
	 * @return CveItem with relevant items registered as properties
	 */
	private static CveItem parseItem(JsonElement je) {
		
		JsonObject cve = je.getAsJsonObject()
						   .get("cve").getAsJsonObject();
		JsonObject config = je.getAsJsonObject()
				   		      .get("configurations").getAsJsonObject();
		JsonObject impact = je.getAsJsonObject()
				   		   	  .get("impact").getAsJsonObject();
		JsonObject cvssV3 = null;
		JsonObject cvssV2 = null;
		
		if (impact.has("baseMetricV3")) {
			cvssV3 = impact.get("baseMetricV3").getAsJsonObject()
		   		   	  	   .get("cvssV3").getAsJsonObject();
		}
		if (impact.has("baseMetricV2")) {
			cvssV2 = impact.get("baseMetricV2").getAsJsonObject();
		}
		
		String cveId = cve.get("CVE_data_meta").getAsJsonObject()
						  .get("ID").getAsString();
		
		String cweId = cve.get("problemtype").getAsJsonObject()
						  .get("problemtype_data").getAsJsonArray()
						  .get(0).getAsJsonObject()
						  .get("description").getAsJsonArray()
						  .get(0).getAsJsonObject()
						  .get("value").getAsString();
		
		String description = cve.get("description").getAsJsonObject()
				  				.get("description_data").getAsJsonArray()
				  				.get(0).getAsJsonObject()
				  				.get("value").getAsString();
		
		List<CpeMatch> cpeList = new ArrayList<>();
		
		JsonArray cpeJson = config.get("nodes").getAsJsonArray()
								  .get(0).getAsJsonObject()
				  				  .get("cpe_match").getAsJsonArray();
		
		if (cpeJson.size() > 0) {
			for (JsonElement elem : cpeJson) {
				JsonObject obj = elem.getAsJsonObject();
				String uri = obj.get("cpe23Uri").getAsString();
				boolean vulnerable = obj.get("vulnerable").getAsBoolean();
				CpeMatch cpeItem = new CpeMatch(uri, vulnerable);
				if (obj.has("versionStartIncluding")) {
					cpeItem.setvStartIncl(obj.get("versionStartIncluding").getAsString());
				}
				if (obj.has("versionEndIncluding")) {
					cpeItem.setvEndIncl(obj.get("versionEndIncluding").getAsString());
				}
				if (obj.has("versionStartExcluding")) {
					cpeItem.setvStartExcl(obj.get("versionStartExcluding").getAsString());
				}
				if (obj.has("versionEndExcluding")) {
					cpeItem.setvEndExcl(obj.get("versionEndExcluding").getAsString());
				}
				cpeList.add(cpeItem);
			}
		}
		
		Integer baseScoreV3 = null;
		String baseSeverityV3 = null;
		
		if (cvssV3 != null) {
			baseScoreV3 = cvssV3.get("baseScore").getAsInt();
			baseSeverityV3 = cvssV3.get("baseSeverity").getAsString();
		}
		
		Integer baseScoreV2 = null;
		String baseSeverityV2 = null;
		
		if (cvssV2 != null) {
			baseScoreV2 = cvssV2.get("cvssV2").getAsJsonObject()
								.get("baseScore").getAsInt();
			baseSeverityV2 = cvssV2.get("severity").getAsString();
		}
		
		String published = je.getAsJsonObject()
				   			 .get("publishedDate").getAsString();
		
		String modified = je.getAsJsonObject()
	   			 			.get("lastModifiedDate").getAsString();
		
		CveItem item = new CveItem(cveId, cweId, description, cpeList, 
								   baseScoreV3, baseSeverityV3, baseScoreV2, 
								   baseSeverityV2, published, modified);
		
		return item;
	}

}
