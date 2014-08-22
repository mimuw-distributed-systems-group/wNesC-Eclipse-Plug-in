package pl.edu.mimuw.nesc.plugin.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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

	public NescProjectBuilder() {
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		/* Enable the user to reparse the entire project. */
		ProjectManager.recreateProjectContext(getProject(), true);
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
		// TODO
	}

	/**
	 * Resource delta visitor which helps to walk resources tree.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 */
	private final class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		private final IProject project;

		public ResourceDeltaVisitor(IProject project) {
			this.project = project;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			// TODO
			return true;
		}
	}
}
