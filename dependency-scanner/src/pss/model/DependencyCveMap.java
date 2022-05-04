package pss.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
	
//	public void removeDuplicates() {
//		Map<Dependency, List<CveItem>> duplicateFree = new HashMap<>();
//		boolean identical = false;
//		for (Dependency dep : this.dependencyMap.keySet()) {
//			identical = false;
//			if (duplicateFree.isEmpty()) {
//				duplicateFree.put(dep, this.dependencyMap.get(dep));
//			} else {
//				for (Dependency dep2 : duplicateFree.keySet()) {
//					if (dep.equals(dep2)) {
//						identical = true;
//					}
//				}
//				if (!identical) {
//					duplicateFree.put(dep, this.dependencyMap.get(dep));
//				}
//			}
//		}
//		this.dependencyMap = duplicateFree;
//	}
	
	public void removeDuplicates() {
		List<Dependency> duplicates;
		List<Dependency> rest = new ArrayList<>(this.dependencyMap.keySet());
		Dependency newest = null;
		while (!rest.isEmpty()) {
			Dependency dep = rest.remove(0);
			duplicates = rest.stream()
					.filter(x -> (x.equals(dep)))
					.collect(Collectors.toList());
			if (!duplicates.isEmpty()) {
				newest = dep;
				for (Dependency dup : duplicates) {
					if (dup.getLastChecked().after(newest.getLastChecked())) {
						this.dependencyMap.remove(newest);
						newest = dup;
					} else {
						this.dependencyMap.remove(dup);
					}
				}
				rest.removeAll(duplicates);
			}
		}
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
		List<CveItem> compareAgainst = null;
		for (Map.Entry<Dependency, List<CveItem>> entry : dependencyMap.entrySet()) {
			for (Map.Entry<Dependency, List<CveItem>> entry2 : deleted.getDependencyMap().entrySet()) {
				if (entry.getKey().equals(entry2.getKey())) {
					compareAgainst = entry2.getValue();
				}
			}
			if (compareAgainst == null) {
				nonDeleted.put(entry.getKey(), this.dependencyMap.get(entry.getKey()));
			} else {
				Map<String, CveItem> map = compareAgainst.stream()
						.collect(Collectors.toMap(c -> c.getCveId(), c -> c));
				List<CveItem> itemsNotDeleted = entry.getValue().stream()
						.filter(c -> map.get(c.getCveId()) == null)
						.collect(Collectors.toList());
				if (itemsNotDeleted.size() > 0) {
					nonDeleted.put(entry.getKey(), itemsNotDeleted);
				}
			}
			compareAgainst = null;
		}
		this.dependencyMap = nonDeleted;
	}
	
	public void removeNonApplicableVersion() {
		for (Map.Entry<Dependency, List<CveItem>> entry : dependencyMap.entrySet()) {
			List<CveItem> items = new ArrayList<>();
			for (CveItem item : entry.getValue()) {
				List<CpeMatch> validCPEs = new ArrayList<>();
				for (CpeMatch cpe : item.getCpeList()) {
					if (cpe.appliesToVersion(entry.getKey().getVersion())) {
						validCPEs.add(cpe);
					}
				}
				if (!validCPEs.isEmpty()) {
					item.setCpeList(validCPEs);
					items.add(item);
				}
			}
			dependencyMap.put(entry.getKey(), items);
		}
	}
	
	public List<CveItem> getMapValue(Dependency key) {
		return this.dependencyMap.get(key);
	}
	
	public void mergeMaps(Map<Dependency, List<CveItem>> secondMap) {
		this.dependencyMap.putAll(secondMap);
		removeDuplicates();
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
