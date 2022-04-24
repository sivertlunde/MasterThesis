package dependencyscanner.actions;

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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.Workbench;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dependency_scanner.Activator;
import dependencyscanner.preferences.PreferenceConstants;
import dependencyscanner.util.StorageUtil;
import pss.model.CveItem;
import pss.model.Dependency;
import pss.model.DependencyCveMap;
import pss.util.NvdDataFetcher;

public class ScanDependenciesAction implements IObjectActionDelegate {
	
		private static IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		private static final Integer VALID = store.getInt(PreferenceConstants.P_DAYS);
		private DependencyCveMap validFromStorage = new DependencyCveMap();

	    /** The current selection. */
	    protected ISelection selection;

	    /** true if this action is used from editor */
	    protected boolean usedInEditor;

	    private IWorkbenchPart targetPart;

	    @Override
	    public final void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
	        this.targetPart = targetPart;
	    }

	    @Override
	    public final void selectionChanged(final IAction action, final ISelection newSelection) {
	        if (!usedInEditor) {
	            this.selection = newSelection;
	        }
	    }

	    @Override
	    public void run(final IAction action) {
	    	Job job = new Job("Dependency scanner") { 
			    @Override 
			    protected IStatus run(IProgressMonitor monitor) { 
			        monitor.beginTask("Checking for vulnerabilities ...", 100); 
			        int test = VALID;
			        updateDependencyMap();
			        monitor.done(); 
			        return Status.OK_STATUS; 
			    } 
			}; 
			job.schedule();
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
				if (this.validFromStorage.getDependencyMap() != null) {
					this.validFromStorage.mergeMaps(dependencyMap);
				} else {
					this.validFromStorage.setDependencyMap(dependencyMap);
				}
				StorageUtil.storeData(this.validFromStorage);
			}
		}
		
		private IProject getCurrentProject(){    
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
			if (fromStorage != null) {
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
			} else {
				return dependencies;
			}
		}
	    
}
