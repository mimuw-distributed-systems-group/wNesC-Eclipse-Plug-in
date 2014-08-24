package pl.edu.mimuw.nesc.plugin.projects.util;

import static pl.edu.mimuw.nesc.plugin.projects.util.EnvironmentVariableResolver.resolveTosDirVariable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
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

	private static final String INCLUDES = "includes";
	private static final String MACROS = "macros";
	private static final String PATHS = "paths";

	/**
	 * Gets names of platforms "hardcoded" in plugin settings.
	 *
	 * @return predefined platforms.
	 */
	public static String[] getPredefinedPlatforms() {
		List<String> result;
		try {
			result = getPredefinedPlatformsDir();
		} catch (IOException e) {
			result = new ArrayList<>();
			e.printStackTrace();
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Gets names of user-defined platforms.
	 *
	 * @return user-defined platforms
	 */
	public static String[] getUserDefinedPlatforms() {
		final IPreferenceStore store = NescPlugin.getDefault().getPreferenceStore();
		final File file = new File(store.getString(NescPluginPreferences.PLATFORMS_DIR));
		final File[] files = file.listFiles();
		final List<String> result = new ArrayList<>();
		if (files == null) {
			return new String[0];
		} else {
			for (File f : files) {
				if (f.isFile()) {
					result.add(FileUtils.getFileNameWithoutExtension(f.getPath()));
				}
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
		final Configuration config;
		if (predefinedPlatform) {
			final URL url = new URL("platform:/plugin/Nesc_Plugin/resources/predefined_platforms/" + platformName
					+ ".properties");
			config = new PropertiesConfiguration(url);
		} else {
			final IPreferenceStore store = NescPlugin.getDefault().getPreferenceStore();
			final File file = new File(store.getString(NescPluginPreferences.PLATFORMS_DIR) + File.separator
					+ platformName + ".properties");
			config = new PropertiesConfiguration(file);
		}

		final String[] includes = resolveTosDirVariable(config.getStringArray(INCLUDES), tinyOsPath);
		final String[] macros = config.getStringArray(MACROS);
		final String[] paths = resolveTosDirVariable(config.getStringArray(PATHS), tinyOsPath);
		return new NescPlatform(paths, includes, macros);
	}

	private static List<String> getPredefinedPlatformsDir() throws IOException {
		final URL url = new URL("platform:/plugin/Nesc_Plugin/resources/predefined_platforms/platforms");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

		final List<String> platforms = new ArrayList<>();
		String line = null;
		while ((line = br.readLine()) != null) {
			platforms.add(line);
		}
		br.close();
		return platforms;
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
