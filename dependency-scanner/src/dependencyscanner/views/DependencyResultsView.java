package dependencyscanner.views;


import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.IActionBars;
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


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class DependencyResultsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "dependencyscanner.views.SampleView";

	@Inject IWorkbench workbench;
	
	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action action3;
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
		workbench.getHelpSystem().setHelp(viewer.getControl(), "dependency-scanner.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
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

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
//		fillLocalPullDown(bars.getMenuManager());
//		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action3);
	}

	private void makeActions() {
		action1 = new Action() {
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
		action1.setText("Remove");
		action1.setToolTipText("Remove dependency from list");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(workbench.getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action3 = new Action() {
			public void run() {
				browser.back();
			}
		};
		action3.setText("Go back");
		action3.setToolTipText("Go back to the previous page in the browser");
		
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				Dependency dep = (Dependency) obj;
				List<CveItem> items = data.getMapValue(dep);
				String html = HtmlUtil.generateDependencyHtml(dep, items);
				browser.setText(html);
				currentlyInBrowser = dep;
//				showMessage("Double-click detected on "+obj.toString());
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
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
