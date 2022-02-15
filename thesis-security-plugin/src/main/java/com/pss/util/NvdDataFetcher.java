package com.pss.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.pss.model.CveItem;

public class NvdDataFetcher {
	
	private final static String NVD_API_URL = "https://services.nvd.nist.gov/rest/json/cves/1.0/?cpeMatchString=cpe:2.3:::";
	
	public static Map<String, List<CveItem>> fetchData(List<String> queryStrings) {
		
		Map<String, List<CveItem>> vulnerableDependencyMap = new HashMap<>();
		
		for(String queryString : queryStrings) {
			JsonObject jo = DataFetcher.fetchDataFromUrl(NVD_API_URL + queryString);
			List<CveItem> items = CustomJsonParser.parseCveItems(queryString, jo);
			vulnerableDependencyMap.put(queryString, items);
		}
		
		return vulnerableDependencyMap;
	}
	

}
