package dependencyscanner.actions;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

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
