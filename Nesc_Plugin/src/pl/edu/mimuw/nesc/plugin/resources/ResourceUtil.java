package pl.edu.mimuw.nesc.plugin.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

/**
 * Utility class with methods for extracting data involving project's resources.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class ResourceUtil {

	/**
	 * Gets list of all subdirectories of given project.
	 *
	 * @param project
	 *            current project
	 * @return subdirectories
	 */
	public static List<String> getProjectDirectories(IProject project) {
		final List<String> directories = new ArrayList<>();
		final String rootPath = project.getLocation().toOSString();
		final File rootFile = new File(rootPath);
		directories.add(getDisplayString(rootPath, rootPath));
		visitDirectory(rootPath, rootFile, directories);
		return directories;
	}

	private static void visitDirectory(String projectPath, File rootFile, List<String> directories) {
		final File[] files = rootFile.listFiles();
		if (files == null) {
			return;
		}
		for (File child : files) {
			if (child.isDirectory()) {
				final String path = child.getAbsolutePath();
				final String projectDir = getDisplayString(projectPath, path);
				directories.add(projectDir);
				visitDirectory(projectPath, child, directories);
			}
		}
	}

	private static String getDisplayString(String rootPath, String filePath) {
		final StringBuilder builder = new StringBuilder();
		builder.append("PROJECT_LOC");
		final String shortenPath = filePath.substring(rootPath.length());
		if (!shortenPath.startsWith(File.separator)) {
			builder.append(File.separator);
		}
		builder.append(shortenPath);
		return builder.toString();
	}
}
