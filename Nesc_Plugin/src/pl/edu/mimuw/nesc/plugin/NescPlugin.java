package pl.edu.mimuw.nesc.plugin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import pl.edu.mimuw.nesc.ContextRef;
import pl.edu.mimuw.nesc.Frontend;
import pl.edu.mimuw.nesc.NescFrontend;
import pl.edu.mimuw.nesc.ProjectData;
import pl.edu.mimuw.nesc.plugin.editor.util.AutosaveListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class NescPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "NesC_Plugin"; //$NON-NLS-1$

	// The shared instance
	private static NescPlugin plugin;
	
	// Frontend data
	private static ConcurrentMap<String, ContextRef> projectContext = new ConcurrentHashMap<String, ContextRef>();
	private static ConcurrentMap<String, ProjectData> projectData = new ConcurrentHashMap<String, ProjectData>();
	
	private static Frontend nescFrontend = NescFrontend.builder().build();

	/**
	 * The constructor
	 */
	public NescPlugin() {
		plugin = this;
	}

	/*
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// Adds document auto-save on active document change
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
	
	public ContextRef getProjectContext(String projectName) {
		return projectContext.get(projectName);
	}
	
	public void setProjectContext(String projectName, ContextRef projectContextRef) {
		projectContext.put(projectName, projectContextRef);
	}
	
	public ProjectData getProjectData(String projectName) {
		return projectData.get(projectName);
	}
	
	public void setProjectData(String projectName, ProjectData newProjectData) {
		projectData.put(projectName, newProjectData);
	}
	
	public Frontend getNescFrontend() {
		return nescFrontend;
	}

}
