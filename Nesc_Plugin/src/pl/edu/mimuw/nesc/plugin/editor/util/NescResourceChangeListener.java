package pl.edu.mimuw.nesc.plugin.editor.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;

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
		return IResourceChangeEvent.PRE_DELETE;
	}

	private NescResourceChangeListener() {
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		final IResource resource = event.getResource();

		switch (event.getType()) {
		case IResourceChangeEvent.PRE_DELETE:
			if (resource instanceof IProject) {
				// FIXME: use logger
				System.out.println("Removing project " + resource.getName());
				final String projectName = resource.getName();
				ProjectManager.deleteProject(projectName);
			}
		}
	}

}
