package dependencyscanner.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import dependencyscanner.util.StorageUtil;
import dependencyscanner.util.WorkspaceUtil;
import dependencyscanner.views.DependencyResultsView;

public class OpenResultsTabAction implements IObjectActionDelegate {

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
    	String projectName = WorkspaceUtil.getName(selection);
    	StorageUtil.fetchData(projectName, true);
    	WorkspaceUtil.openResultsTab();
    	
    }

}
