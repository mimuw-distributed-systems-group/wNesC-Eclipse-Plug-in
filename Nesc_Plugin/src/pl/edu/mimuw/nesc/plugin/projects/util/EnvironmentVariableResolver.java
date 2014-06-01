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

	// FIXME:
	// http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2FresAdv_builders.htm

	private static final String TOSDIR = "\\$\\{TOSDIR\\}";//$NON-NLS-1$

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
			result[i] = paths[i].replaceAll(TOSDIR, tinyOsPath);
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
	 * @return list of paths with resolved <code>TOSDIR</code> variable reference
	 */
	public static List<String> resolveTosDirVariable(List<String> paths, String tinyOsPath) {
		final List<String> result = new ArrayList<>(paths.size());
		for (String path : paths) {
			result.add(path.replaceAll(TOSDIR, tinyOsPath));
		}
		return result;
	}
}
