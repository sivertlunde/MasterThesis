package dependencyscanner.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dependency_scanner.Activator;
import pss.model.DependencyCveMap;

public class StorageUtil {
	
	private static Activator plugin = Activator.getDefault();
	private static final String dir = plugin.getStateLocation().toOSString();
	private static final String separator = System.getProperty("file.separator");
	private static final String filePath = dir + separator + "data.txt";
	
	public static boolean storeData(DependencyCveMap map) {
		try (FileOutputStream fos = new FileOutputStream(filePath, false);
			 ObjectOutputStream oos = new ObjectOutputStream(fos)) {

			    oos.writeObject(map);
			    return true;
			} catch (IOException ex) {
			    ex.printStackTrace();
			    return false;
			}
		
	}
	
	public static DependencyCveMap fetchData() {
		try (FileInputStream fis = new FileInputStream(filePath); 
				ObjectInputStream ois = new ObjectInputStream(fis)) {

			DependencyCveMap map = (DependencyCveMap) ois.readObject();
			return map;
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
