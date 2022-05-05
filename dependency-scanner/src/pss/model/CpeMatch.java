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
	
	public boolean appliesToVersion(String version) {
		if (version == null) {
			return true;
		}
		String[] versionNumbers = version.split("\\.");
		String start = null;
		boolean startIncl = false;
		String end = null;
		boolean endIncl = false;
		if (vStartIncl != null && !vStartIncl.isEmpty()) {
			start = vStartIncl;
			startIncl = true;
		}
		if (vStartExcl != null && !vStartExcl.isEmpty()) {
			start = vStartExcl;
			startIncl = false;
		}
		if (vEndIncl != null && !vEndIncl.isEmpty()) {
			end = vEndIncl;
			endIncl = true;
		}
		if (vEndExcl != null && !vEndExcl.isEmpty()) {
			end = vEndExcl;
			endIncl = false;
		}
		if (start == null && end == null) {
			String cpeVersion = uri.split("\\:")[5];
			start = cpeVersion;
			end = cpeVersion;
			startIncl = true;
			endIncl = true;
		}
		return compareVersion(versionNumbers, start, end, startIncl, endIncl);
	}
	
	private boolean compareVersion(String[] version, String start, String end, boolean startIncl, boolean endIncl) {
		String[] startNumbers = start != null ? start.split("\\.") : new String[0];
		String[] endNumbers = end != null ? end.split("\\.") : new String[0];
		boolean startChecked = false;
		boolean endChecked = false;
		int vLength = version.length;
		int sLength = startNumbers.length;
		int eLength = endNumbers.length;
		for (int i = 0; i < vLength; i++) {
			if (!isNumeric(version[i])) {
				return true;
			}
			int ver = Integer.parseInt(version[i]);
			int s = startNumbers.length > i ? Integer.parseInt(startNumbers[i]) : -1;
			int e = endNumbers.length > i ? Integer.parseInt(endNumbers[i]) : -1;
			if (!startChecked && ver > s) {
				startChecked = true;
			}
			if (!endChecked && ver < e) {
				endChecked = true;
			}
			if (startChecked && endChecked) {
				return true;
			}
			if ((startIncl && endIncl) || hasNotReachedSomeLastIndex(i, vLength, sLength, eLength)) {
				if (!((ver == s || startChecked) && (e == -1 || ver == e || endChecked))) {
					return false;
				}
			} else if (startIncl) {
				if (!((ver == s || startChecked) && (e == -1 || endChecked || (ver == e && i+1 < eLength)))) {
					return false;
				}
			} else if (endIncl) {
				if (!((startChecked || (ver == s && i+1 < vLength)) && (e == -1 || ver == e || endChecked))) {
					return false;
				}
			} else {
				if (!((startChecked || (ver == s && i+1 < vLength)) && (e == -1 || endChecked || (ver == e && i+1 < eLength)))) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean hasNotReachedSomeLastIndex(int i, int v, int s, int e) {
		int x = i+1;
		return x < v && x < s && x < e;
	}
	
	private boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        Integer.parseInt(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
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
