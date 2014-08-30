package pl.edu.mimuw.nesc.plugin.resources;

import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences;

/**
 * Helper class for handling project's source paths.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class PathsUtil {

	/**
	 * Returns the list of non-platform include paths of given project.
	 * Environment variables will not be resolved.
	 *
	 * @param project
	 *            current project
	 * @return non-platform include paths
	 */
	public static List<Path> getNonPlatformPaths(IProject project) {
		/* Previously saved values. */
		List<String> nonPlatformPaths = getProjectPreferenceValueStringList(project, NON_PLATFORM_INCLUDE_PATHS);
		/* Paths specified explicitly by the user. Should always be on the list. */
		final List<String> additionalPaths = getProjectPreferenceValueStringList(project, ADDITIONAL_INCLUDE_PATHS);
		/* List of dirs automatically extracted from the project hierarchy. */
		final List<String> projectPaths = ResourceUtil.getProjectDirectories(project);
		/* Paths checked by the user. */
		final List<String> checkedPaths = getProjectPreferenceValueStringList(project, ACTIVE_INCLUDE_PATHS);

		/* If nonPlatformPaths is empty, this is probably a fresh project
		 * and proper settings were not set yet. */
		if (nonPlatformPaths.isEmpty()) {
			/* By default all additional and all subfolders should be checked. */
			nonPlatformPaths.addAll(additionalPaths);
			nonPlatformPaths.addAll(projectPaths);
			checkedPaths.addAll(projectPaths);
		}

		final List<Path> resultPaths = new ArrayList<>();

		/* Iterate old paths to preserve the order. */
		for (String path : nonPlatformPaths) {
			if (!additionalPaths.contains(path) && !projectPaths.contains(path)) {
				// path will be removed
				continue;
			}
			final Path newPath = new Path(path, path, checkedPaths.contains(path), additionalPaths.contains(path));
			resultPaths.add(newPath);
		}

		/* Newly created directories are appended to the end of the list. */
		for (String path : projectPaths) {
			if (!nonPlatformPaths.contains(path)) {
				final Path newPath = new Path(path, path, true, false);
				resultPaths.add(newPath);
			}
		}

		return resultPaths;
	}

	/**
	 * Saves the list of non-platform source paths of given project.
	 *
	 * @param project
	 *            current project
	 * @param paths
	 *            paths to be saved
	 * @throws BackingStoreException
	 *             when settings cannot be properly saved
	 */
	public static void saveNonPlatformPaths(IProject project, List<Path> paths) throws BackingStoreException {
		final List<String> nonPlatformPaths = new ArrayList<>();
		final List<String> additionalPaths = new ArrayList<>();
		final List<String> activePaths = new ArrayList<>();

		for (Path path : paths) {
			final String value = path.getValue();
			nonPlatformPaths.add(value);
			if (path.isCustom()) {
				additionalPaths.add(value);
			}
			if (path.isActive()) {
				activePaths.add(value);
			}
		}

		NescProjectPreferences.transaction(project)
				.set(NON_PLATFORM_INCLUDE_PATHS, nonPlatformPaths)
				.set(ADDITIONAL_INCLUDE_PATHS, additionalPaths)
				.set(ACTIVE_INCLUDE_PATHS, activePaths)
				.commit();
	}

	/**
	 * Refreshes project source folders list. Should be called each time folders
	 * hierarchy has changed.
	 *
	 * @param project
	 *            current project
	 * @throws BackingStoreException
	 *             when settings cannot be properly saved
	 */
	public static void refreshProjectDirectories(IProject project) throws BackingStoreException {
		saveNonPlatformPaths(project, getNonPlatformPaths(project));
	}

	public static List<String> resolvePaths(IProject project, List<String> paths) throws URISyntaxException {
		final List<String> result = new ArrayList<>();
		for (String path : paths) {
			result.add(project.getPathVariableManager().resolveURI(new URI(path)).getPath());
		}
		return result;
	}

	public static String[] resolvePaths(IProject project, String[] paths) throws URISyntaxException {
		final String[] result = new String[paths.length];
		for (int i = 0; i < paths.length; ++i) {
			result[i] = project.getPathVariableManager().resolveURI(new URI(paths[i])).getPath();
		}
		return result;
	}

	/**
	 * Returns the list of non-platform include paths of given project with
	 * resolved environment variables.
	 *
	 * @param project
	 *            current project
	 * @return list of resolved non-platform source paths
	 * @throws URISyntaxException
	 */
	public static List<String> getResolvedNonPlatformPaths(IProject project) throws URISyntaxException {
		final List<Path> nonPlatformPaths = getNonPlatformPaths(project);
		List<String> additionalPaths = new ArrayList<>();
		for (Path path : nonPlatformPaths) {
			if (!path.isActive()) {
				continue;
			}
			additionalPaths.add(path.getValue());
		}
		return resolvePaths(project, additionalPaths);
	}

	/**
	 * Represents project's source directory.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	public static final class Path {
		private final String displayString;
		private final String value;
		private final boolean active;
		/**
		 * <code>true</code> if the path is user-defined, <code>false</code> if
		 * the path is project's subdirectory.
		 */
		private final boolean custom;

		public Path(String displayString, String value, boolean active, boolean custom) {
			this.displayString = displayString;
			this.value = value;
			this.active = active;
			this.custom = custom;
		}

		public String getDisplayString() {
			return displayString;
		}

		public String getValue() {
			return value;
		}

		public boolean isActive() {
			return active;
		}

		public boolean isCustom() {
			return custom;
		}
	}

}
