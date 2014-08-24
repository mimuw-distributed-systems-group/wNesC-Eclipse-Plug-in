package pl.edu.mimuw.nesc.plugin;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import pl.edu.mimuw.nesc.Frontend;
import pl.edu.mimuw.nesc.NescFrontend;
import pl.edu.mimuw.nesc.plugin.editor.util.AutosaveListener;
import pl.edu.mimuw.nesc.plugin.editor.util.NescResourceChangeListener;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 */
public class NescPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "NesC_Plugin";

	private static Frontend NESC_FRONTEND = NescFrontend.builder().build();

	// The shared instance
	private static NescPlugin plugin;

	public NescPlugin() {
		plugin = this;
	}

	@Override
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
		ResourcesPlugin.getWorkspace().addResourceChangeListener(NescResourceChangeListener.getInstance(),
				NescResourceChangeListener.getHandledEvents());
		buildWorkspace();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.getWorkbench().removeWindowListener(AutosaveListener.getInstance());
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(NescResourceChangeListener.getInstance());
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

	/**
	 * Gets NesC frontend instance.
	 *
	 * @return NesC frontend
	 */
	public Frontend getNescFrontend() {
		return NESC_FRONTEND;
	}

	private void buildWorkspace() {
		final Job job = new Job("Building workspace...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					return Status.OK_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.BUILD);
		job.schedule();
	}
}
