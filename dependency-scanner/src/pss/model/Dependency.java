package pss.model;

import java.io.Serializable;
import java.util.Date;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Dependency implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String groupId;
	private String artifactId;
	private String version;
	private Date lastChecked;

	public Dependency() {}
	
	public Dependency(String groupId, String artifactId, String version, Date lastChecked) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.lastChecked = lastChecked;
	}

	public Dependency(Node node) {
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			String name = nodes.item(i).getNodeName();
			if ("groupId".equals(name)) {
				this.groupId = nodes.item(i).getFirstChild().getNodeValue();
			} else if ("artifactId".equals(name)) {
				this.artifactId = nodes.item(i).getFirstChild().getNodeValue();
			} else if ("version".equals(name)) {
				this.version = nodes.item(i).getFirstChild().getNodeValue();
			}
		}
		this.lastChecked = new Date();
	}
	
	public boolean equals(Dependency d) {
		if (this.groupId.equals(d.groupId) 
				&& this.artifactId.equals(d.artifactId) 
				&& ((this.version == null && d.version == null)
					|| 
					(this.version != null 
					&& d.version != null 
					&& this.version.equals(d.version)
					))
			) {
			return true;
		}
		return false;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(Date lastChecked) {
		this.lastChecked = lastChecked;
	}

	@Override
	public String toString() {
		return "Dependency [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version
				+ ", lastChecked=" + lastChecked + "]";
	}

}
