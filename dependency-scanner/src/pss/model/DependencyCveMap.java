package pss.model;

import java.io.Serializable;
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