package dependencyscanner.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import dependency_scanner.Activator;
import pss.model.CveItem;
import pss.model.Dependency;
import pss.model.DependencyCveMap;

public class StorageUtil {

	private static Activator plugin = Activator.getDefault();
	private static final String dir = plugin.getStateLocation().toOSString();
	private static final String separator = System.getProperty("file.separator");

	/*
	 * Every time data is stored, a copy is stored under "default", overwriting the
	 * previously stored data each time. This way, the plug-in can fetch the data
	 * which was most recently fetched without knowing the project name.
	 */
	public static boolean storeData(DependencyCveMap map, String project) {
		return storeToLocation(map, project) && storeToLocation(map, "default");
	}

	public static boolean storeToLocation(DependencyCveMap map, String location) {
		String filePath = dir + separator + location + separator + "data.txt";
		File file = new File(filePath);
		file.getParentFile().mkdirs();
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (FileOutputStream fos = new FileOutputStream(filePath, false);
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {

			oos.writeObject(map);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static DependencyCveMap fetchData(String project, boolean updateDefault) {
		String filePath = dir + separator + project + separator + "data.txt";
		try (FileInputStream fis = new FileInputStream(filePath); ObjectInputStream ois = new ObjectInputStream(fis)) {

			DependencyCveMap map = (DependencyCveMap) ois.readObject();
			if (updateDefault && !"default".equals(project)) {
				storeToLocation(map, "default");
			}
			return map;
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (updateDefault) {
			storeToLocation(null, "default");
		}
		return null;
	}
	
	public static boolean updateDeleted(Dependency dep, List<CveItem> items) {
		DependencyCveMap deleted = fetchData("deleted", false);
		if (deleted != null) {
			Dependency fromDeleted = deleted.containsKey(dep);
			if (fromDeleted != null) {
				List<CveItem> deletedItems = deleted.getMapValue(fromDeleted);
				deletedItems.addAll(items);
				deleted.getDependencyMap().put(fromDeleted, deletedItems);
			} else {
				deleted.getDependencyMap().put(dep, items);
			}
		} else {
			deleted = new DependencyCveMap();
			Map<Dependency, List<CveItem>> map = new HashMap<>();
			map.put(dep, items);
			deleted.setDependencyMap(map);
		}
		return storeToLocation(deleted, "deleted");
	}
	
	public static DependencyCveMap fetchDeleted() {
		return fetchData("deleted", false);
	}

	// Fetching the most recently stored data
	public static DependencyCveMap fetchData() {
		return fetchData("default", false);
	}

}
