package dependencyscanner.views;


import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import dependencyscanner.util.HtmlUtil;
import dependencyscanner.util.MarkerUtil;
import dependencyscanner.util.StorageUtil;
import dependencyscanner.util.WorkspaceUtil;
import pss.model.CveItem;
import pss.model.Dependency;
import pss.model.DependencyCveMap;

public class DependencyResultsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "dependencyscanner.views.SampleView";

	@Inject IWorkbench workbench;
	
	private TableViewer viewer;
	private Action removeAction;
	private Action doubleClickAction;
	private Browser browser;
	
	private DependencyCveMap data;
	private Dependency currentlyInBrowser = null;
	 

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		@Override
		public Image getImage(Object obj) {
			return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	
	public void updateView() {
		viewer.setInput(createModel());
		if (data != null) {
			browser.setText("Double-click a vulnerability in the list to view more information");
		} else {
			browser.setText("");
		}
		
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		
		TableViewerColumn groupColumn = createColumnFor(viewer, "GroupID");
		groupColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((Dependency) element).getGroupId();
			}
		});
		
		TableViewerColumn artifactColumn = createColumnFor(viewer, "ArtifactID");
		artifactColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((Dependency) element).getArtifactId();
			}
		});
		
		TableViewerColumn versionColumn = createColumnFor(viewer, "Version");
		versionColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((Dependency) element).getVersion();
			}
		});
		viewer.setInput(createModel());
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		
		// Create the help context id for the viewer's control
//		workbench.getHelpSystem().setHelp(viewer.getControl(), "dependency-scanner.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		browser = new Browser(parent, SWT.BORDER);
		if (data != null) {
			browser.setText("Double-click a vulnerability in the list to view more information");
		}

	}
	
	private TableViewerColumn createColumnFor(TableViewer viewer, String label) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(label);
		column.getColumn().setMoveable(true);
		return column;
	}
	
	private Dependency[] createModel() {
		data = StorageUtil.fetchData();
		if (data == null) {
			return new Dependency[0];
		}
		data.removeDuplicates();
		data.removeNonVulnerableDependencies();
		DependencyCveMap deleted = StorageUtil.fetchDeleted();
		if (deleted != null) {
			data.removeDeletedDependencies(deleted);
		}
		Set<Dependency> dep = data.getDependencyMap().keySet();
		Dependency[] depArray = new Dependency[dep.size()];
		dep.toArray(depArray);
		return depArray;
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DependencyResultsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(removeAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void makeActions() {
		removeAction = new Action() {
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				viewer.remove(obj);				
				Dependency dep = (Dependency) obj;
				StorageUtil.updateDeleted(dep, data.getMapValue(dep));
				IFile file = WorkspaceUtil.getFileFromProject("pom.xml", dep.getProject());
				MarkerUtil.deleteMarkerForDependency(dep, file);
				if (currentlyInBrowser == dep) {
					if (viewer.getInput() != null) {
						browser.setText("Double-click a vulnerability in the list to view more information");
					} else {
						browser.setText("");
					}
					currentlyInBrowser = null;
				}
			}
		};
		removeAction.setText("Remove");
		removeAction.setToolTipText("Remove dependency from list");
		removeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE));
		
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				Dependency dep = (Dependency) obj;
				List<CveItem> items = data.getMapValue(dep);
				String html = HtmlUtil.generateDependencyHtml(dep, items);
				browser.setText(html);
				currentlyInBrowser = dep;
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
