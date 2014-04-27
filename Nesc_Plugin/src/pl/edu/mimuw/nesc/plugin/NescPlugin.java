package pl.edu.mimuw.nesc.plugin;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import pl.edu.mimuw.nesc.plugin.editor.util.AutosaveListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class NescPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "NesC_Plugin"; //$NON-NLS-1$

	// The shared instance
	private static NescPlugin plugin;

	/**
	 * The constructor
	 */
	public NescPlugin() {
	}

	/*
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.getWorkbench().addWindowListener(AutosaveListener.getInstance());
		for (IWorkbenchWindow window : this.getWorkbench().getWorkbenchWindows()) {
			window.addPageListener(AutosaveListener.getInstance());
			for (IWorkbenchPage page : window.getPages()) {
				page.addPartListener(AutosaveListener.getInstance());
			}
		}
		plugin = this;
	}

	/*
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.getWorkbench().removeWindowListener(AutosaveListener.getInstance());
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static NescPlugin getDefault() {
		return plugin;
	}

}
