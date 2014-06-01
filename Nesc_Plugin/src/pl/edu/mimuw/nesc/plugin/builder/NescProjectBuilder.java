package pl.edu.mimuw.nesc.plugin.builder;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.plugin.marker.MarkerHelper;
import pl.edu.mimuw.nesc.plugin.projects.util.ProjectManager;

/**
 * Schedules project builds according to the set of modified resources since
 * last build execution (ResourceDelta).
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescProjectBuilder extends IncrementalProjectBuilder {

	/*
	 * http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2FresAdv_builders.htm
	 */

	public static final String BUILDER_ID = "pl.edu.mimuw.nesc.plugin.builder.NescProjectBuilder";

	private final MarkerHelper markerHelper;

	public NescProjectBuilder() {
		this.markerHelper = new MarkerHelper();
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		System.out.println("Build project; kind = " + kind);

		final IProject project = getProject();

		switch (kind) {
		case INCREMENTAL_BUILD:
		case AUTO_BUILD:
			final IResourceDelta delta = getDelta(project);
			partialBuild(project, delta);
			break;
		case FULL_BUILD:
			fullBuild(project);
			break;
		}
		System.out.println("Build end; kind = " + kind);
		return null;
	}

	private void partialBuild(IProject project, IResourceDelta delta) throws CoreException {
		if (delta == null) {
			return;
		}
		delta.accept(new ResourceDeltaVisitor(project));
	}

	private void fullBuild(final IProject project) {
		System.out.println("Full build");
		final Job job = new Job("Building project...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// TODO: handle errors
				ProjectManager.recreateProjectContext(project);
				try {
					markerHelper.updateMarkers(project);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.BUILD);
		job.schedule();
	}

	/**
	 * Resource delta visitor which helps to walk resources tree.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 */
	private final class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		private static final String SETTINGS_DIR_PATH_SUFFIX = ".settings";

		private final IProject project;

		public ResourceDeltaVisitor(IProject project) {
			this.project = project;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			final IResource resource = delta.getResource();
			final IPath path = resource.getRawLocation();

			if (resource.getType() == IResource.FOLDER) {
				/*
				 * Check if project preferences were modified. We need to
				 * perform full build.
				 */
				if (path.lastSegment().equals(SETTINGS_DIR_PATH_SUFFIX)) {
					fullBuild(project);
					/* Settings directory - no need to visit children. */
					return false;
				}
			} else if (resource.getType() == IResource.FILE) {
				System.out.println("Update file.");
				try {
					final Job job = new Job("Parsing and analysing file...") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								updateFile(project, path, (IFile) resource);
							} catch (CoreException e) {
								e.printStackTrace();
							}
							return Status.OK_STATUS;
						}
					};
					job.setPriority(Job.SHORT);
					job.schedule();
				} catch (Exception e) {
					e.printStackTrace();
				}
				/* FILE - no children. */
				return false;
			}
			/* DIRECTORY OR ROOT - visit children. */
			return true;
		}

		private void updateFile(IProject project, IPath path, IFile file) throws CoreException {
			final FileData data = ProjectManager.updateFile(project, path.toOSString());
			markerHelper.updateMarkers(project, file, data);
		}
	}
}
