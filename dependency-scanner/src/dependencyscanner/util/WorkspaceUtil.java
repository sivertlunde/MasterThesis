package dependencyscanner.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class WorkspaceUtil {
	
	public static String getName(ISelection selection) {
		String project = getProjectName(selection);
		if (!project.equals("default")) {
			return getWorkspaceName() + "/" + project;
		} else {
			return "default";
		}
		
	}
	
	public static String getProjectName(ISelection selection) {
		IProject currentProject = getCurrentProject(selection);
		try {
			IProjectDescription desc = currentProject.getDescription();
			return desc.getName();
		} catch (CoreException e) {
			e.printStackTrace();
			return "default";
		}
	}
	
	public static String getWorkspaceName() {
		String workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		int lastSlashIndex = workspaceDir.lastIndexOf('/');
		return workspaceDir.substring(lastSlashIndex + 1);
	}
     
    public static IProject getCurrentProject(ISelection selection) {
		IProject project = null;
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();

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
    
    public static IProject getProjectByName(String name) {
    	return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }
    
    public static IFile getFileFromProject(String fileName, String projectName) {
    	return getProjectByName(projectName).getFile(fileName);
    }

}
