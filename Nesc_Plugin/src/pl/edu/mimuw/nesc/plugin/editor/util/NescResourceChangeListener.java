package pl.edu.mimuw.nesc.plugin.editor.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.plugin.marker.MarkerHelper;
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

	private final MarkerHelper markerHelper;

	private NescResourceChangeListener() {
		this.markerHelper = new MarkerHelper();
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

				/* Settings directory. */
				if (path.lastSegment().equals(".settings")) {
					rebuildProject(project);
					return false;
				}

				/*
				 * NOTE: When any chold of a folder was added, removed etc., but
				 * the folder itself was not modified, therefore the
				 * "delta.getKind()" is set to MODIFIED for the folder. In
				 * consequence, when only a file in the folder is changed, the
				 * project settings will not be updated
				 * (refreshProjectDirectories will not be called), since none of
				 * the source paths was changed.
				 *
				 * On the other hand, when folder is created / removed / moved /
				 * renamed one of the following flags will be set: ADDED,
				 * REMOVED, MOVED_FROM, MOVED_TO (rename = remove + add).
				 */
				if (kind == IResourceDelta.ADDED || kind == IResourceDelta.MOVED_FROM
						|| kind == IResourceDelta.MOVED_TO || kind == IResourceDelta.REMOVED) {
					refreshProjectDirectories(project);
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

	private void rebuildProject(final IProject project) {
		final Job job = new Job("Rebuilding project...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// TODO: handle errors
				System.out.println("Recreate project context...");
				ProjectManager.recreateProjectContext(project, true);
				try {
					markerHelper.updateMarkers(project);
					updateFiles(project);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	private void updateFiles(IProject project) throws CoreException {
		IResourceVisitor visitor = new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) throws CoreException {
				final int type = resource.getType();
				final IPath path = resource.getFullPath();
				final IProject project = resource.getProject();

				if (type == IResource.FILE) {
					final IFile file = (IFile) resource;
					final FileData data = ProjectManager.getFileData(project, path.toOSString());
					if (data != null) {
						markerHelper.updateMarkers(project, file, data);
					}
				}
				return true;
			}
		};
		project.accept(visitor);
	}


	private void refreshProjectDirectories(final IProject project) {
		final Job job = new Job("Updating source folder settings...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					System.out.println("Updating the list of source directories...");
					PathsUtil.refreshProjectDirectories(project);
					/*
					 * Rebuild will be automatically invoked when the change
					 * listener is called with POST_CHANGE flag.
					 */
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
}
