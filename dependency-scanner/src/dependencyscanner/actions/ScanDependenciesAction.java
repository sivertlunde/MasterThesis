package dependencyscanner.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dependency_scanner.Activator;
import dependencyscanner.preferences.PreferenceConstants;
import dependencyscanner.util.MarkerUtil;
import dependencyscanner.util.PositionalXMLReader;
import dependencyscanner.util.StorageUtil;
import dependencyscanner.util.WorkspaceUtil;
import dependencyscanner.views.DependencyResultsView;
import pss.model.CveItem;
import pss.model.Dependency;
import pss.model.DependencyCveMap;
import pss.util.NvdDataFetcher;

public class ScanDependenciesAction implements IObjectActionDelegate {

	private static IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	private static final Integer VALID = store.getInt(PreferenceConstants.P_DAYS);
	private DependencyCveMap validDependencyData = new DependencyCveMap();
	private IProject currentProject;
	private IFile pom;
	private String projectID = "default";
	private String projectName = "noname";

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
				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
				updateDependencyMap(subMonitor);
				MarkerUtil.deleteMarkers(pom);
				MarkerUtil.addMarkers(pom, validDependencyData);
				IMarker[] markers = MarkerUtil.getMarkers(pom);
				openResultsTab();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}

	private void updateDependencyMap(SubMonitor subMonitor) {
		subMonitor.setTaskName("Fetching dependencies from pom.xml...");
		initiateVariables();
		NodeList nodes = parseXmlAndGetNodes(pom, "dependency");
		List<Dependency> dependencies = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Dependency dep = new Dependency(nodes.item(i));
			dep.setProject(projectName);
			dependencies.add(dep);
		}
		
		subMonitor.setWorkRemaining(90);
		SubMonitor storageMonitor = subMonitor.split(30);
		storageMonitor.setTaskName("Fetching data from storage...");
		
		List<Dependency> toBeChecked = getValidDataFromStorage(dependencies);
		storageMonitor.setWorkRemaining(0);
		SubMonitor fetchMonitor = subMonitor.split(60);
		fetchMonitor.setTaskName("Updating vulnerability data...");
		if (!toBeChecked.isEmpty()) {
			Map<Dependency, List<CveItem>> dependencyMap = NvdDataFetcher.fetchData(toBeChecked);
			if (this.validDependencyData.getDependencyMap() != null) {
				this.validDependencyData.mergeMaps(dependencyMap);
			} else {
				this.validDependencyData.setDependencyMap(dependencyMap);
			}
			
			StorageUtil.storeData(this.validDependencyData, projectID);
		} else {
			StorageUtil.storeToLocation(validDependencyData, "default");
		}
		fetchMonitor.setWorkRemaining(0);
	}
	
	private void initiateVariables() {
		currentProject = WorkspaceUtil.getCurrentProject(selection);
		pom = currentProject.getFile("pom.xml");
		projectID = WorkspaceUtil.getName(selection);
		projectName = WorkspaceUtil.getProjectName(selection);
	}
	
	private void openResultsTab() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart view = page.findView("dependencyscanner.views.DependencyResultsView");
				if (view != null && view instanceof DependencyResultsView) {
					DependencyResultsView myView = (DependencyResultsView)view;
					myView.updateView();
				} else {
					try {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.showView("dependencyscanner.views.DependencyResultsView");
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private static NodeList parseXmlAndGetNodes(IFile file, String tagName) {
		try (InputStream is = file.getContents()) {
			Document doc = PositionalXMLReader.readXML(is);
			Element element = doc.getDocumentElement();
			return element.getElementsByTagName(tagName);
		} catch (SAXException | CoreException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<Dependency> getValidDataFromStorage(List<Dependency> dependencies) {
		DependencyCveMap fromStorage = StorageUtil.fetchData(projectID, false);
		if (fromStorage != null) {
			Map<Dependency, List<CveItem>> validMap = new HashMap<>();
			List<Dependency> notValid = new ArrayList<>();
			Date today = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(today);
			c.add(Calendar.DAY_OF_MONTH, -VALID);
			Date validAfter = c.getTime();
			for (Dependency d : dependencies) {
				Dependency dep = fromStorage.containsKey(d);
				if (dep != null && (dep.getLastChecked().after(validAfter))) {
					validMap.put(dep, fromStorage.getMapValue(dep));
				} else {
					notValid.add(d);
				}
			}
			validDependencyData.setDependencyMap(validMap);
			return notValid;
		} else {
			return dependencies;
		}
	}

}
