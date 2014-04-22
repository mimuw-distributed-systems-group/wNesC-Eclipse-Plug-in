package pl.edu.mimuw.nesc.plugin.wizards.fields;

import pl.edu.mimuw.nesc.plugin.natures.NescProjectNature;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Class that filter all projects that are not a nesC projects. It follows
 * the Singleton design pattern.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

final class NescProjectsFilter extends ViewerFilter {
    /**
     * The only instance of this class.
     */
    private static final NescProjectsFilter instance = new NescProjectsFilter();

    /**
     * @return The only instance of this class.
     */
    public static NescProjectsFilter getInstance() {
        return instance;
    }

    /**
     * Private constructor because of using the Singleton design pattern.
     */
    private NescProjectsFilter() {}

    @Override
    public boolean select(Viewer viewer, Object parent, Object element) {
        if (element instanceof IProject) {
            final IProject project = (IProject) element;
            try {
                return project.hasNature(NescProjectNature.NATURE_ID);
            } catch(CoreException e) {
                /* the project is filtered if an exception is thrown */
                return false;
            }
        }

        return element instanceof IFolder;
    }
}
