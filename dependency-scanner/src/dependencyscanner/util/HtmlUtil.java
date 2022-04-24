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
	
	private static void getVulnerableDependencies() {
		
	}
	
	public static boolean generateHtmlFile(String fileName) {
		data = StorageUtil.fetchData();
		data.removeDuplicates();
        File f = new File(dir + separator + fileName);
        BufferedWriter bw;
        try {
        	bw = new BufferedWriter(new FileWriter(f));
        	bw.write("<html><body><h1>Vulnerable dependencies</h1>");
            bw.write("<div>");
            for (Map.Entry<Dependency, List<CveItem>> entry : data.getDependencyMap().entrySet()) {
            	if (!entry.getValue().isEmpty()) {
            		generateDependencyHtml(bw, entry.getKey(), entry.getValue());
            	}
            }
            bw.write("</div>");
            bw.write("</body></html>");
            bw.close();
            return true;
        } catch(IOException e) {
        	e.printStackTrace();
        	return false;
        }
        
    }
	
	private static void generateDependencyHtml(BufferedWriter bw, Dependency dep, List<CveItem> items) throws IOException {
		bw.write("<div style=\"display:flex;\">");
		bw.write("<h4 style=\"width:33%\">" + 
				 "GroupId: " + dep.getGroupId() + 
				 "</h4>");
		bw.write("<h4 style=\"width:33%\">" + 
				 "ArtifactId: " + dep.getArtifactId() +
				 "</h4>");
		bw.write("<h4 style=\"width:33%\">" + 
				 "Version: " + dep.getVersion() + 
				 "</h4>");
		bw.write("</div>");
		for (CveItem item : items) {
			generateCveItemHtml(bw, item);
		}
	}
	
	private static void generateCveItemHtml(BufferedWriter bw, CveItem item) throws IOException {
		String severity = item.getBaseSeverityV3() != null ? item.getBaseSeverityV3() : item.getBaseSeverityV2();
		Integer severityScore = item.getBaseScoreV3() != null ? item.getBaseScoreV3() : item.getBaseScoreV2();
		bw.write("<div style=\"border: 1px solid; border-radius: 4px\">");
		bw.write("<p style=\"margin:5px\">" + 
				 item.getCveId() + 
				 "</p>");
		bw.write("<p style=\"margin:5px\">" + 
				 item.getDescription() + 
				 "</p>");
		bw.write("<p style=\"margin:5px\">" + 
				 "Severity: <span style=\"color:red\">" + severity + " " + severityScore + 
				 "</span></p>");
		if (item.getCweId() != null) {
			bw.write("<p style=\"margin:5px\">" + 
					 "CWE: <a href=\"https://cwe.mitre.org/data/definitions/" + 
					item.getCweId().toLowerCase().replaceAll("cwe-", "") + ".html\">" + item.getCweId() +
					 "</a></p>");
		}
		
		bw.write("</div>");
	}
	
	public static String getFilePath(String fileName) {
		return dir + separator + fileName;
	}

}
