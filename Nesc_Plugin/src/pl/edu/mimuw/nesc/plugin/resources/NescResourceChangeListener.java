package pl.edu.mimuw.nesc.plugin.resources;

import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.MAIN_CONFIGURATION;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.getProjectPreferenceValue;

import java.io.File;

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
import pl.edu.mimuw.nesc.common.util.file.FileUtils;
import pl.edu.mimuw.nesc.plugin.frontend.FrontendManager;
import pl.edu.mimuw.nesc.plugin.marker.MarkerHelper;
import pl.edu.mimuw.nesc.plugin.natures.NescProjectNature;

import com.google.common.base.Optional;

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
			try {
				if (((IProject) resource).hasNature(NescProjectNature.NATURE_ID)) {
					System.out.println("Removing project " + resource.getName());
					final String projectName = resource.getName();
					FrontendManager.deleteContext(projectName);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private void postChange(IResourceChangeEvent event) {
		IResourceDelta rootDelta = event.getDelta();
		ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
		try {
			rootDelta.accept(visitor);
			if (visitor.getPostAction() != null) {
				visitor.getPostAction().run();
			}
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
				FrontendManager.updateContext(project);
				FrontendManager.buildContext(project);
				try {
					// marker are updated automatically after project rebuild
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
					final Optional<FileData> data = FrontendManager.getFileData(project, path.toOSString());
					if (data.isPresent()) {
						MarkerHelper.updateMarkers(project, file, data.get());
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

	private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		private Runnable postAction;

		public Runnable getPostAction() {
			return postAction;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			final int kind = delta.getKind();
			final int type = delta.getResource().getType();
			final IPath path = delta.getResource().getFullPath();
			final IPath fullPath = delta.getResource().getLocation();
			final IProject project = delta.getResource().getProject();

			if (type == IResource.ROOT) {
				return true;
			}

			if (!project.exists() || !project.isOpen() || !project.hasNature(NescProjectNature.NATURE_ID)) {
				return false;
			}

			/*
			 * If root configuration is added/removed, rebuild project.
			 */
			if (type == IResource.FILE && wasResourceModified(kind)) {
				final String projectMainDir = project.getLocation().toOSString();
				final String mainConfigName = getProjectPreferenceValue(project, MAIN_CONFIGURATION);
				final String mainConfigPath = projectMainDir + File.separator + mainConfigName + ".nc";
				System.out.println(mainConfigPath + " ? " + path.toString());
				if (FileUtils.normalizePath(mainConfigPath).equals(fullPath.toOSString())) {
					postAction = new Runnable() {
						@Override
						public void run() {
							rebuildProject(project);
						}
					};
				}
			}

			if (type != IResource.FOLDER) {
				return true;
			}

			/* Settings directory. */
			if (path.lastSegment().equals(".settings")) {
				postAction = new Runnable() {
					@Override
					public void run() {
						rebuildProject(project);
					}
				};
				return false;
			}

			/*
			 * NOTE: When any child of a folder was added, removed etc., but the
			 * folder itself was not modified, therefore the "delta.getKind()"
			 * is set to MODIFIED for the folder. In consequence, when only a
			 * file in the folder is changed, the project settings will not be
			 * updated (refreshProjectDirectories will not be called), since
			 * none of the source paths was changed.
			 *
			 * On the other hand, when folder is created / removed / moved /
			 * renamed one of the following flags will be set: ADDED, REMOVED,
			 * MOVED_FROM, MOVED_TO (rename = remove + add).
			 */
			if (wasResourceModified(kind)) {
				if (postAction == null) {
					postAction = new Runnable() {
						@Override
						public void run() {
							refreshProjectDirectories(project);
						}
					};
				}
			}
			return true;
		}

		private boolean wasResourceModified(int kind) {
			return (kind == IResourceDelta.ADDED || kind == IResourceDelta.MOVED_FROM
					|| kind == IResourceDelta.MOVED_TO || kind == IResourceDelta.REMOVED);
		}
	};
}
