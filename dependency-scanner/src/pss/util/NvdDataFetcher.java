package pss.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import pss.model.CveItem;
import pss.model.Dependency;

public class NvdDataFetcher {
	
	private final static String NVD_API_URL = "https://services.nvd.nist.gov/rest/json/cves/1.0/?cpeMatchString=cpe:2.3:::";
	
	public static Map<Dependency, List<CveItem>> fetchData(List<Dependency> dependencies) {
		
		Map<Dependency, List<CveItem>> vulnerableDependencyMap = new HashMap<>();
		
		for(Dependency d : dependencies) {
			JsonObject jo = DataFetcher.fetchDataFromUrl(NVD_API_URL + d.getArtifactId());
			if (jo != null) {
				List<CveItem> items = CustomJsonParser.parseCveItems(jo);
				vulnerableDependencyMap.put(d, items);
			}
		}
		return vulnerableDependencyMap;
	}
}
