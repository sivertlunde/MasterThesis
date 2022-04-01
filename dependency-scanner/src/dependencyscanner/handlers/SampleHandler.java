package dependencyscanner.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.internal.Workbench;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dependencyscanner.util.StorageUtil;
import pss.model.CveItem;
import pss.model.Dependency;
import pss.model.DependencyCveMap;
import pss.util.NvdDataFetcher;

public class SampleHandler extends AbstractHandler {
	
	private static final Integer VALID = -7;
	private DependencyCveMap validFromStorage = new DependencyCveMap();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("Dependency scanner") { 
		    @Override 
		    protected IStatus run(IProgressMonitor monitor) { 
		        monitor.beginTask("Checking for vulnerabilities ...", 100); 
		        updateDependencyMap();
		        monitor.done(); 
		        return Status.OK_STATUS; 
		    } 
		}; 
		job.schedule();
		
//		HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().showView(viewId);
//		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
//		MessageDialog.openInformation(window.getShell(), "Dependency-scanner-2", "placeholder");
		
		return null;
	}
	
	private void updateDependencyMap() {
		IProject current = getCurrentProject();
		IFile pom = current.getFile("pom.xml");
		NodeList nodes = parseXmlAndGetNodes(pom, "dependency");
		List<Dependency> dependencies = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i ++) {
			dependencies.add(new Dependency(nodes.item(i)));
		}
		List<Dependency> toBeChecked = getValidDataFromStorage(dependencies);
		if (!toBeChecked.isEmpty()) {
			Map<Dependency, List<CveItem>> dependencyMap = NvdDataFetcher.fetchData(toBeChecked);
			this.validFromStorage.mergeMaps(dependencyMap);
			StorageUtil.storeData(this.validFromStorage);
		}
	}
	
	private static IProject getCurrentProject(){    
        ISelectionService selectionService =     
            Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();    

        ISelection selection = selectionService.getSelection();    

        IProject project = null;    
        if(selection instanceof IStructuredSelection) {    
            Object element = ((IStructuredSelection)selection).getFirstElement();    

			if (element instanceof IResource) {
				project = ((IResource) element).getProject();
			} else if (element instanceof PackageFragmentRootContainer) {
				IJavaProject jProject = ((PackageFragmentRootContainer) element).getJavaProject();
				project = jProject.getProject();
			} else if (element instanceof IJavaElement) {
				IJavaProject jProject = ((IJavaElement) element).getJavaProject();
				project = jProject.getProject();
			}
        }     
        return project;    
    }
	
	private static NodeList parseXmlAndGetNodes(IFile file, String tagName) {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try (InputStream is = file.getContents()) {
			
			DocumentBuilder db = dbf.newDocumentBuilder();

	        Document doc = db.parse(is);
	        
	        Element element = doc.getDocumentElement();
	        
	        return element.getElementsByTagName(tagName);
	        
		} catch (ParserConfigurationException | SAXException | CoreException | IOException e) {
	          e.printStackTrace();
	    }
		
		return null;
	}
	
	private List<Dependency> getValidDataFromStorage(List<Dependency> dependencies) {
		DependencyCveMap fromStorage = StorageUtil.fetchData();
		Map<Dependency, List<CveItem>> validMap = new HashMap<>();
		List<Dependency> notValid = new ArrayList<>();
		Date today = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(today);
		c.add(Calendar.DAY_OF_MONTH, VALID);
		Date validAfter = c.getTime();
		for (Dependency d : dependencies) {
			Dependency dep = fromStorage.containsKey(d);
			if (dep != null && (dep.getLastChecked().after(validAfter))) {
				validMap.put(dep, fromStorage.getMapValue(dep));
			} else {
				notValid.add(d);
			}
		}
		validFromStorage.setDependencyMap(validMap);
		return notValid;
	}
}
