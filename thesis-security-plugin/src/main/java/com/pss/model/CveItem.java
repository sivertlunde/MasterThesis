package com.pss.model;

import com.google.gson.JsonObject;

public class CveItem {

	private String artifactId;
	private String cveId;
	private JsonObject cveData;
	
	public CveItem() {}
	
	public CveItem(String artifactId, String cveId, JsonObject cveData) {
		this.artifactId = artifactId;
		this.cveId = cveId;
		this.cveData = cveData;
	}

	public JsonObject getValueForKey(String key) {
		return cveData.getAsJsonObject(key);
	}
	
	public String getArtifactId() {
		return artifactId;
	}
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	public String getCveId() {
		return cveId;
	}
	public void setCveId(String cveId) {
		this.cveId = cveId;
	}
	public JsonObject getCveData() {
		return cveData;
	}
	public void setCveData(JsonObject cveData) {
		this.cveData = cveData;
	}

}
