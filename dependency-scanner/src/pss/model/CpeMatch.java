package pss.model;

import java.io.Serializable;

public class CpeMatch implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String uri;
	private boolean vulnerable;
	private String vStartIncl;
	private String vEndIncl;
	private String vStartExcl;
	private String vEndExcl;
	
	public CpeMatch() {}
	
	public CpeMatch(String uri, boolean vulnerable) {
		this.uri = uri;
		this.vulnerable = vulnerable;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean isVulnerable() {
		return vulnerable;
	}

	public void setVulnerable(boolean vulnerable) {
		this.vulnerable = vulnerable;
	}

	public String getvStartIncl() {
		return vStartIncl;
	}

	public void setvStartIncl(String vStartIncl) {
		this.vStartIncl = vStartIncl;
	}

	public String getvEndIncl() {
		return vEndIncl;
	}

	public void setvEndIncl(String vEndIncl) {
		this.vEndIncl = vEndIncl;
	}

	public String getvStartExcl() {
		return vStartExcl;
	}

	public void setvStartExcl(String vStartExcl) {
		this.vStartExcl = vStartExcl;
	}

	public String getvEndExcl() {
		return vEndExcl;
	}

	public void setvEndExcl(String vEndExcl) {
		this.vEndExcl = vEndExcl;
	}

	@Override
	public String toString() {
		return "CpeMatch [uri=" + uri + ", vulnerable=" + vulnerable + ", vStartIncl=" + vStartIncl + ", vEndIncl="
				+ vEndIncl + ", vStartExcl=" + vStartExcl + ", vEndExcl=" + vEndExcl + "]";
	}
	
}
