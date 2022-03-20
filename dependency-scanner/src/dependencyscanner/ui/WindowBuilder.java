package dependencyscanner.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;


public class WindowBuilder {
	
	private Display display;
	private Shell shell;

	public WindowBuilder() {
		this.display = PlatformUI.getWorkbench().getDisplay();
		this.shell = display.getActiveShell();
		TabFolder folder = new TabFolder(shell, SWT.NONE);
		TabItem tab1 = new TabItem(folder, SWT.NONE);
	    tab1.setText("Tab 1");
		
	}
	
	
}
