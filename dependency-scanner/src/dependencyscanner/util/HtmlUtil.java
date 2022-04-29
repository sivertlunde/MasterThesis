package dependencyscanner.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import dependency_scanner.Activator;
import pss.model.CveItem;
import pss.model.Dependency;
import pss.model.DependencyCveMap;

public class HtmlUtil {
	
	private static Activator plugin = Activator.getDefault();
	private static final String dir = plugin.getStateLocation().toOSString();
	private static final String separator = System.getProperty("file.separator");
	
	private static DependencyCveMap data;
	
	public static boolean generateHtmlFile(String fileName) {
		
        File file = new File(dir + separator + fileName + separator + "results.html");
        file.getParentFile().mkdirs();
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        BufferedWriter bw;
        try {
        	bw = new BufferedWriter(new FileWriter(file));
        	bw.write(generateHtmlFileString(fileName));
            bw.close();
            return true;
        } catch(IOException e) {
        	e.printStackTrace();
        	return false;
        }
        
    }
	
	private static String generateHtmlFileString(String fileName) {
		data = StorageUtil.fetchData(fileName, false);
		data.removeDuplicates();
		data.removeNonVulnerableDependencies();
		DependencyCveMap deleted = StorageUtil.fetchDeleted();
		if (deleted != null) {
			data.removeDeletedDependencies(deleted);
		}
		StringBuilder html = new StringBuilder();
		html.append("<html><body><h1>Vulnerable dependencies</h1>");
		html.append("<div>");
		for (Map.Entry<Dependency, List<CveItem>> entry : data.getDependencyMap().entrySet()) {
			html.append(generateDependencyHtml(entry.getKey(), entry.getValue()));
        }
		html.append("</div>");
		html.append("</body></html>");
		return html.toString();
	}
	
	public static String generateDependencyHtml(Dependency dep, List<CveItem> items) {
		StringBuilder html = new StringBuilder();
		html.append("<div style=\"display:flex;\">");
		html.append("<h4 style=\"width:33%\">" + 
				 "GroupId: " + dep.getGroupId() + 
				 "</h4>");
		html.append("<h4 style=\"width:33%\">" + 
				 "ArtifactId: " + dep.getArtifactId() +
				 "</h4>");
		html.append("<h4 style=\"width:33%\">" + 
				 "Version: " + dep.getVersion() != null ? dep.getVersion() : "" + 
				 "</h4>");
		html.append("</div>");
		for (CveItem item : items) {
			html.append(generateCveItemHtml(item));
		}
		return html.toString();
	}
	
	private static String generateCveItemHtml(CveItem item) {
		StringBuilder html = new StringBuilder();
		String severity = item.getBaseSeverityV3() != null ? item.getBaseSeverityV3() : item.getBaseSeverityV2();
		Integer severityScore = item.getBaseScoreV3() != null ? item.getBaseScoreV3() : item.getBaseScoreV2();
		String color = getTextColor(severity);
		String backgroundColor = getBackgroundColor(severity);
		html.append("<div style=\"border: 1px solid; border-radius: 4px\">");
		html.append("<p style=\"margin:5px\">" + 
				 item.getCveId() + 
				 "</p>");
		html.append("<p style=\"margin:5px\">" + 
				 item.getDescription() + 
				 "</p>");
		html.append("<p style=\"margin:5px\">" + 
				 "Severity: <span style=\"color:" + color + ";background-color:" 
				+ backgroundColor + "\">" + severity + " " + severityScore + 
				 "</span></p>");
		if (item.getCweId() != null) {
			html.append("<p style=\"margin:5px\">" + 
					 "CWE: <a href=\"https://cwe.mitre.org/data/definitions/" + 
					item.getCweId().toLowerCase().replaceAll("cwe-", "") + ".html\">" + item.getCweId() +
					 "</a></p>");
		}
		
		html.append("</div>");
		return html.toString();
	}
	
	public static String getFilePath(String fileName) {
		return dir + separator + fileName + separator + "results.html";
	}
	
	private static String getBackgroundColor(String severity) {
		if (severity.toLowerCase().equals("low")) {
			return "yellow";
		} else if (severity.toLowerCase().equals("medium")) {
			return "orange";
		} else if (severity.toLowerCase().equals("high")) {
			return "red";
		} else {
			return "black";
		}
	}
	
	private static String getTextColor(String severity) {
		return severity.toLowerCase().equals("critical") ? "#ECECEC" : "black";
	}

}
