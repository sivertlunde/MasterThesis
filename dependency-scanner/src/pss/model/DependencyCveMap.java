package pss.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyCveMap implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Map<Dependency, List<CveItem>> dependencyMap;
	
	public DependencyCveMap() {}
	
	public DependencyCveMap(Map<Dependency, List<CveItem>> dependencyMap) {
		this.dependencyMap = dependencyMap;
	}
	
	public void add(Dependency key, List<CveItem> value) {
		this.dependencyMap.put(key, value);
	}
	
	public Dependency containsKey(Dependency d) {
		Set<Dependency> keys = this.dependencyMap.keySet();
		for (Dependency dep : keys) {
			if (dep.equals(d)) {
				return dep;
			}
		}
		return null;
	}
	
	public void removeDuplicates() {
		Map<Dependency, List<CveItem>> duplicateFree = new HashMap<>();
		boolean identical = false;
		for (Dependency dep : this.dependencyMap.keySet()) {
			identical = false;
			if (duplicateFree.isEmpty()) {
				duplicateFree.put(dep, this.dependencyMap.get(dep));
			} else {
				for (Dependency dep2 : duplicateFree.keySet()) {
					if (dep.equals(dep2)) {
						identical = true;
					}
				}
				if (!identical) {
					duplicateFree.put(dep, this.dependencyMap.get(dep));
				}
			}
		}
		this.dependencyMap = duplicateFree;
	}
	
	public void removeNonVulnerableDependencies() {
		Map<Dependency, List<CveItem>> vulnerableMap = new HashMap<>();
		for (Map.Entry<Dependency, List<CveItem>> entry : dependencyMap.entrySet()) {
        	if (!entry.getValue().isEmpty()) {
        		vulnerableMap.put(entry.getKey(), entry.getValue());
        	}
        }
		this.dependencyMap = vulnerableMap;
	}
	
	public void removeDeletedDependencies(DependencyCveMap deleted) {
		Map<Dependency, List<CveItem>> nonDeleted = new HashMap<>();
		boolean found = false;
		for (Dependency dep : this.dependencyMap.keySet()) {
			for (Dependency dep2 : deleted.getDependencyMap().keySet()) {
				if (dep.equals(dep2)) {
					found = true;
				}
			}
			if (!found) {
				nonDeleted.put(dep, this.dependencyMap.get(dep));
			}
		}
		this.dependencyMap = nonDeleted;
	}
	
	public List<CveItem> getMapValue(Dependency key) {
		return this.dependencyMap.get(key);
	}
	
	public void mergeMaps(Map<Dependency, List<CveItem>> secondMap) {
		this.dependencyMap.putAll(secondMap);
	}

	public Map<Dependency, List<CveItem>> getDependencyMap() {
		return dependencyMap;
	}

	public void setDependencyMap(Map<Dependency, List<CveItem>> dependencyMap) {
		this.dependencyMap = dependencyMap;
	}

	@Override
	public String toString() {
		return "DependencyCveMap [dependencyMap=" + dependencyMap + "]";
	}
	

}
