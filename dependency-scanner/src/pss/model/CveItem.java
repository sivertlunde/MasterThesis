package pss.model;

import java.io.Serializable;
import java.util.List;

public class CveItem implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String cveId;
	private String cweId;
	private String description;
	private List<CpeMatch> cpeList;
	private Integer baseScoreV3;
	private String baseSeverityV3;
	private Integer baseScoreV2;
	private String baseSeverityV2;
	private String published;
	private String modified;
	
	public CveItem() {}

	public CveItem(String cveId, String cweId, String description, List<CpeMatch> cpeList, Integer baseScoreV3,
			String baseSeverityV3, Integer baseScoreV2, String baseSeverityV2, String published, String modified) {
		super();
		this.cveId = cveId;
		this.cweId = cweId;
		this.description = description;
		this.cpeList = cpeList;
		this.baseScoreV3 = baseScoreV3;
		this.baseSeverityV3 = baseSeverityV3;
		this.baseScoreV2 = baseScoreV2;
		this.baseSeverityV2 = baseSeverityV2;
		this.published = published;
		this.modified = modified;
	}

	public String getCveId() {
		return cveId;
	}

	public void setCveId(String cveId) {
		this.cveId = cveId;
	}

	public String getCweId() {
		return cweId;
	}

	public void setCweId(String cweId) {
		this.cweId = cweId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<CpeMatch> getCpeList() {
		return cpeList;
	}

	public void setCpeList(List<CpeMatch> cpeList) {
		this.cpeList = cpeList;
	}

	public Integer getBaseScoreV3() {
		return baseScoreV3;
	}

	public void setBaseScoreV3(Integer baseScoreV3) {
		this.baseScoreV3 = baseScoreV3;
	}

	public String getBaseSeverityV3() {
		return baseSeverityV3;
	}

	public void setBaseSeverityV3(String baseSeverityV3) {
		this.baseSeverityV3 = baseSeverityV3;
	}

	public Integer getBaseScoreV2() {
		return baseScoreV2;
	}

	public void setBaseScoreV2(Integer baseScoreV2) {
		this.baseScoreV2 = baseScoreV2;
	}

	public String getBaseSeverityV2() {
		return baseSeverityV2;
	}

	public void setBaseSeverityV2(String baseSeverityV2) {
		this.baseSeverityV2 = baseSeverityV2;
	}

	public String getPublished() {
		return published;
	}

	public void setPublished(String published) {
		this.published = published;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	@Override
	public String toString() {
		return "CveItem [cveId=" + cveId + ", cweId=" + cweId + ", description=" + description + ", cpeList=" + cpeList
				+ ", baseScoreV3=" + baseScoreV3 + ", baseSeverityV3=" + baseSeverityV3 + ", baseScoreV2=" + baseScoreV2
				+ ", baseSeverityV2=" + baseSeverityV2 + ", published=" + published + ", modified=" + modified + "]";
	}

}
