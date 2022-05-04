package dependencyscanner.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import pss.model.Dependency;
import pss.model.DependencyCveMap;

public class MarkerUtil {
	
	private static final String MARKER_TYPE = "dependency-scanner.vulnerableDependency";
	
	public static void addMarkers(IFile file, DependencyCveMap map) {
		map.removeNonVulnerableDependencies();
		map.removeDuplicates();
		DependencyCveMap deleted = StorageUtil.fetchDeleted();
		if (deleted != null) {
			map.removeDeletedDependencies(deleted);
		}
		for (Dependency dep : map.getDependencyMap().keySet()) {
			createMarker(file, dep);
		}
	}
	
	public static void createMarker(IFile file, Dependency dep) {
		Map<String, ? super Object> attributes = new HashMap<String, Object>();
 
        attributes.put(IMarker.LINE_NUMBER, dep.getLineNumber());
        attributes.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_WARNING));
        attributes.put(IMarker.MESSAGE, "The dependency [" + dep.getGroupId() + "] may be vulnerable");
        
        try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttributes(attributes);
			marker.setAttribute("artifactId", dep.getArtifactId());
			marker.setAttribute("groupId", dep.getGroupId());
			marker.setAttribute("version", dep.getVersion());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static IMarker[] getMarkers(IFile file) {
		try {
			return file.findMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean deleteMarkers(IFile file) {
		for (IMarker marker : getMarkers(file)) {
			try {
				marker.delete();
			} catch (CoreException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	public static boolean deleteMarkerForDependency(Dependency dep, IFile file) {
		for (IMarker marker : getMarkers(file)) {
			try {
				String artifactId = (String)marker.getAttribute("artifactId");
				String groupId = (String)marker.getAttribute("groupId");
				String version = (String)marker.getAttribute("version");
				Dependency markerDep = new Dependency(groupId, artifactId, version, null, null, null);
				if (markerDep.equals(dep)) {
					marker.delete();
					return true;
				}
			} catch (CoreException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

}
