package dependencyscanner.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OpenResultsAction implements IObjectActionDelegate {

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
    	try {
        	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("dependencyscanner.views.SampleView");
    	} catch(PartInitException e) {
    		e.printStackTrace();
    	}

    }
}
