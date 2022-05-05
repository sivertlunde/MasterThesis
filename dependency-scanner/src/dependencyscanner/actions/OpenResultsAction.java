package dependencyscanner.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import dependencyscanner.util.HtmlUtil;
import dependencyscanner.util.WorkspaceUtil;

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
    	String projectName = WorkspaceUtil.getName(selection);
    	HtmlUtil.generateHtmlFile(projectName);
    	Program p = Program.findProgram("html");
    	p.execute(HtmlUtil.getFilePath(projectName));
    }
    
}
