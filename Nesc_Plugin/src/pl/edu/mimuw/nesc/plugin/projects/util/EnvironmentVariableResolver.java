package pl.edu.mimuw.nesc.plugin.projects.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for resolving environment variable references in paths,
 * strings, etc.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class EnvironmentVariableResolver {

	public static final String TOSDIR = "${TOSDIR}";//$NON-NLS-1$
	public static final String PROJECT_DIR = "${PROJECT_DIR}";//$NON-NLS-1$
	public static final String TOSDIR_REGEXP = "\\$\\{TOSDIR\\}";//$NON-NLS-1$
	public static final String PROJECT_DIR_REGEXP = "\\$\\{PROJECT_DIR\\}";//$NON-NLS-1$

	/**
	 * Replaces all occurrences of <code>TOSDIR</code> variable in given paths
	 * array.
	 *
	 * @param paths
	 *            paths to resolve
	 * @param tinyOsPath
	 *            replacement string
	 * @return array of paths with resolved <code>TOSDIR</code> variable
	 *         reference
	 */
	public static String[] resolveTosDirVariable(String[] paths, String tinyOsPath) {
		final String[] result = new String[paths.length];
		for (int i = 0; i < paths.length; ++i) {
			result[i] = paths[i].replaceAll(TOSDIR_REGEXP, tinyOsPath);
		}
		return result;
	}

	/**
	 * Replaces all occurrences of <code>TOSDIR</code> variable in given paths
	 * list.
	 *
	 * @param paths
	 *            paths to resolve
	 * @param tinyOsPath
	 *            replacement string
	 * @return list of paths with resolved <code>TOSDIR</code> variable
	 *         reference
	 */
	public static List<String> resolveTosDirVariable(List<String> paths, String tinyOsPath) {
		final List<String> result = new ArrayList<>(paths.size());
		for (String path : paths) {
			result.add(path.replaceAll(TOSDIR_REGEXP, tinyOsPath));
		}
		return result;
	}

	/**
	 * Replaces all occurrences of <code>PROJECT_DIR</code> variable in given
	 * path lists.
	 *
	 * @param paths
	 *            paths to resolve
	 * @param projectPath
	 *            replacement string
	 * @return list of paths with resolved <code>PROJECT_DIR</code> variable
	 *         reference
	 */
	public static List<String> resolveProjectDirVariable(List<String> paths, String projectPath) {
		final List<String> result = new ArrayList<>(paths.size());
		for (String path : paths) {
			result.add(path.replaceAll(PROJECT_DIR_REGEXP, projectPath));
		}
		return result;
	}
}
