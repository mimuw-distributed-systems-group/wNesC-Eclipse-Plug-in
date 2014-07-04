package pl.edu.mimuw.nesc.plugin.editor.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.projects.util.PathsUtil;
import pl.edu.mimuw.nesc.plugin.projects.util.ProjectManager;

/**
 * Change listener for NesC projects.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescResourceChangeListener implements IResourceChangeListener {

	private static final NescResourceChangeListener INSTANCE = new NescResourceChangeListener();

	/**
	 * Gets listener singleton.
	 *
	 * @return listener instance
	 */
	public static NescResourceChangeListener getInstance() {
		return INSTANCE;
	}

	/**
	 * Gets the bit-wise OR of all event types of interest to the listener.
	 *
	 * @return event types
	 */
	public static int getHandledEvents() {
		return IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE;
	}

	private NescResourceChangeListener() {
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		switch (event.getType()) {
		case IResourceChangeEvent.PRE_DELETE:
			preDelete(event);
			break;
		case IResourceChangeEvent.POST_CHANGE:
			postChange(event);
			break;
		}
	}

	private void preDelete(IResourceChangeEvent event) {
		final IResource resource = event.getResource();
		if (resource instanceof IProject) {
			// FIXME: use logger
			System.out.println("Removing project " + resource.getName());
			final String projectName = resource.getName();
			ProjectManager.deleteProject(projectName);
		}
	}

	private void postChange(IResourceChangeEvent event) {
		IResourceDelta rootDelta = event.getDelta();
		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				final int kind = delta.getKind();
				final int type = delta.getResource().getType();
				final IPath path = delta.getResource().getFullPath();
				final IProject project = delta.getResource().getProject();

				if (type != IResource.FOLDER) {
					return true;
				}

				/* Settings directory - skip. */
				if (path.lastSegment().equals(".settings")) {
					return false;
				}

				//System.err.println(path + "(type=" + type + ")" + "; changed=" + (kind == IResourceDelta.CHANGED) +
				//		"; added=" + (kind == IResourceDelta.ADDED) + "; removed=" + (kind == IResourceDelta.REMOVED) +
				//		"; moved=" + (kind == IResourceDelta.MOVED_TO));

				if (kind == IResourceDelta.ADDED
						|| kind == IResourceDelta.MOVED_FROM
						|| kind == IResourceDelta.MOVED_TO
						|| kind == IResourceDelta.REMOVED) {
					final Job job = new Job("Updating source folder settings...") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								PathsUtil.refreshProjectDirectories(project);
							} catch (BackingStoreException e) {
								e.printStackTrace();
							}
							return Status.OK_STATUS;
						}
					};
					job.setPriority(Job.INTERACTIVE);
					job.schedule();
				}
				return true;
			}
		};
		try {
			rootDelta.accept(visitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
