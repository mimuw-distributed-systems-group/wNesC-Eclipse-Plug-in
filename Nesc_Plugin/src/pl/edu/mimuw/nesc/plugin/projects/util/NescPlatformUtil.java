package pl.edu.mimuw.nesc.plugin.projects.util;

import java.io.File;
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
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescPlatformUtil {

	private static final String TOSDIR = "\\$\\{TOSDIR\\}";//$NON-NLS-1$

	private static final String INCLUDES = "includes";//$NON-NLS-1$
	private static final String MACROS = "macros";//$NON-NLS-1$
	private static final String PATHS = "paths";//$NON-NLS-1$

	public static String[] getAvailablePlatforms() {
		final List<String> result = new ArrayList<>();

		final File path = new File(getPlatformsDir());
		final File[] files = path.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				result.add(FileUtils.getFileNameWithoutExtension(file.getPath()));
			}
		}

		return result.toArray(new String[result.size()]);
	}

	public static NescPlatform loadPlatformProperties(String platformName, String tinyOsPath)
			throws ConfigurationException {
		final File file = new File(getPlatformsDir() + File.separator + platformName + ".properties");
		final Configuration config = new PropertiesConfiguration(file);
		final String[] includes = config.getStringArray(INCLUDES);
		final String[] macros = config.getStringArray(MACROS);
		final String[] paths = config.getStringArray(PATHS);
		resolveVariables(includes, tinyOsPath);
		resolveVariables(paths, tinyOsPath);
		return new NescPlatform(paths, includes, macros);
	}

	private static String getPlatformsDir() {
		final IPreferenceStore store = NescPlugin.getDefault().getPreferenceStore();
		return store.getString(NescPluginPreferences.PLATFORMS_DIR);
	}

	private static void resolveVariables(String[] values, String tinyOsPath) {
		for (int i = 0; i < values.length; ++i) {
			values[i] = values[i].replaceAll(TOSDIR, tinyOsPath);
		}
	}

	/**
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
