package pl.edu.mimuw.nesc.plugin.projects.util;

import static pl.edu.mimuw.nesc.plugin.projects.util.EnvironmentVariableResolver.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.preference.IPreferenceStore;

import pl.edu.mimuw.nesc.common.util.file.FileUtils;
import pl.edu.mimuw.nesc.plugin.NescPlugin;
import pl.edu.mimuw.nesc.plugin.preferences.NescPluginPreferences;

/**
 * Utility class with methods for retrieving available platforms and loading
 * platform's configuration.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class NescPlatformUtil {

	private static final NescPlatform DUMMY_PLATFORM = new NescPlatform(new String[0], new String[0], new String[0]);

	public static NescPlatform getDummyPlatform() {
		return DUMMY_PLATFORM;
	}

	private static final String INCLUDES = "includes";//$NON-NLS-1$
	private static final String MACROS = "macros";//$NON-NLS-1$
	private static final String PATHS = "paths";//$NON-NLS-1$

	/**
	 * Gets names of platforms "hardcoded" in plugin settings.
	 *
	 * @return predefined platforms.
	 */
	public static String[] getPredefinedPlatforms() {
		final List<String> result = new ArrayList<>();

		final String fullPath;
		try {
			fullPath = getPredefinedPlatformsDir();
		} catch (IOException e) {
			e.printStackTrace();
			return new String[0];
		}
		final File path = new File(fullPath);
		final File[] files = path.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				result.add(FileUtils.getFileNameWithoutExtension(file.getPath()));
			}
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Gets names of user-defined platforms.
	 *
	 * @return user-defined platforms
	 */
	public static String[] getUserDefinedPlatforms() {
		final List<String> result = new ArrayList<>();

		final File path = new File(getUserDefinedPlatformsDir());
		if (!path.exists()) {
			return new String[0];
		}
		final File[] files = path.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				result.add(FileUtils.getFileNameWithoutExtension(file.getPath()));
			}
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Loads settings of platform of given name. Loader tries to find platform
	 * specification file either in predefined platforms directory or in
	 * directory specified by user.
	 *
	 * @param platformName
	 *            platform's name
	 * @param predefinedPlatform
	 *            indicates where platform specification should be searched
	 * @param tinyOsPath
	 *            absolute path to TinyOS sources (to tos/ subdirectory),
	 *            necessary to resolve references to <code>${TOSDIR}</code>
	 *            variable
	 * @return object describing platform
	 * @throws ConfigurationException
	 *             when configuration file is malformed or does not exist
	 * @throws IOException
	 *             when directory of predefined platforms does not exist
	 */
	public static NescPlatform loadPlatformProperties(String platformName, boolean predefinedPlatform, String tinyOsPath)
			throws ConfigurationException, IOException {
		final File file = new File(getPlatformsDir(predefinedPlatform) + File.separator + platformName + ".properties");
		final Configuration config = new PropertiesConfiguration(file);
		final String[] includes = resolveTosDirVariable(config.getStringArray(INCLUDES), tinyOsPath);
		final String[] macros = config.getStringArray(MACROS);
		final String[] paths = resolveTosDirVariable(config.getStringArray(PATHS), tinyOsPath);
		return new NescPlatform(paths, includes, macros);
	}

	private static String getPlatformsDir(boolean predefinedPlatforms) throws IOException {
		if (predefinedPlatforms) {
			return getPredefinedPlatformsDir();
		}
		return getUserDefinedPlatformsDir();
	}

	private static String getPredefinedPlatformsDir() throws IOException {
		final URL url = new URL("platform:/plugin/Nesc_Plugin/resources/predefined_platforms");
		return FileLocator.resolve(url).getPath();
	}

	private static String getUserDefinedPlatformsDir() {
		final IPreferenceStore store = NescPlugin.getDefault().getPreferenceStore();
		return store.getString(NescPluginPreferences.PLATFORMS_DIR);
	}

	/**
	 * Class of objects containing platform-specific settings.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	public static class NescPlatform {

		private String[] paths;
		private String[] files;
		private String[] macros;

		public NescPlatform(String[] paths, String[] files, String[] macros) {
			this.paths = paths;
			this.files = files;
			this.macros = macros;
		}

		public String[] getPaths() {
			return paths;
		}

		public String[] getFiles() {
			return files;
		}

		public String[] getMacros() {
			return macros;
		}
	}

}
