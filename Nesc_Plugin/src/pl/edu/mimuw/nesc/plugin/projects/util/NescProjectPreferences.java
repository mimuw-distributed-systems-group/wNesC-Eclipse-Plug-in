package pl.edu.mimuw.nesc.plugin.projects.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.NescPlugin;

/**
 *
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class NescProjectPreferences {

	public static final String MAIN_CONFIGURATION = "main-configuration";
	public static final String TINYOS_PROJECT = "is-TinyOS-project";
	public static final String TINYOS_PLATFORM = "TinyOS-platform";
	public static final String TINYOS_PREDEFINED_PLATFORM = "pl.edu.mimuw.nesc.plugin.predefined_platform";
	public static final String TINYOS_PATH = "pl.edu.mimuw.nesc.plugin.TinyOS-path";
	public static final String ADDITIONAL_DEFAULT_FILES = "pl.edu.mimuw.nesc.plugin.additional_default_files";
	public static final String ADDITIONAL_PREDEFINED_MACROS = "pl.edu.mimuw.nesc.plugin.additional_predefined_macros";
	public static final String ADDITIONAL_INCLUDE_PATHS = "pl.edu.mimuw.nesc.plugin.additional_include_paths";
	/**
	 * List of include paths which are either project subdirectories or
	 * user-defined paths.
	 */
	public static final String NON_PLATFORM_INCLUDE_PATHS = "pl.edu.mimuw.nesc.plugin.non_platform_include_paths";
	public static final String ACTIVE_INCLUDE_PATHS = "pl.edu.mimuw.nesc.plugin.active_include_paths";
	public static final String COMMENT_HEAD = "pl.edu.mimuw.nesc.plugin.head_comment";
	public static final String COMMENT_ENTITY = "pl.edu.mimuw.nesc.plugin.entity_comment";

	public static TransactionBuilder transaction(IProject project) {
		return new TransactionBuilder(project);
	}

	/**
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 */
	public static class TransactionBuilder {

		private final IEclipsePreferences preferences;

		public TransactionBuilder(IProject project) {
			this.preferences = getProjectPreferences(project);
		}

		public TransactionBuilder set(String key, List<String> value) throws BackingStoreException {
			final String commaSeparatedListString = listToString(value);
			preferences.put(key, commaSeparatedListString);
			return this;
		}

		public TransactionBuilder set(String key, String value) throws BackingStoreException {
			preferences.put(key, value);
			return this;
		}

		public TransactionBuilder set(String key, Integer value) throws BackingStoreException {
			preferences.putInt(key, value);
			return this;
		}

		public TransactionBuilder set(String key, Boolean value) throws BackingStoreException {
			preferences.putBoolean(key, value);
			return this;
		}

		public void commit() throws BackingStoreException {
			preferences.flush();
		}

		public void cancel() {
			// nothing to do
		}
	}

	public static List<String> getProjectPreferenceValueStringList(IProject project, String key) {
		final IEclipsePreferences preferences = getProjectPreferences(project);
		return stringToList(preferences.get(key, ""));
	}

	public static String getProjectPreferenceValue(IProject project, String key) {
		IEclipsePreferences preferences = getProjectPreferences(project);
		return preferences.get(key, "");
	}

	public static Integer getProjectPreferenceValueI(IProject project, String key) {
		IEclipsePreferences preferences = getProjectPreferences(project);
		return preferences.getInt(key, -1);
	}

	public static boolean getProjectPreferenceValueB(IProject project, String key) {
		IEclipsePreferences preferences = getProjectPreferences(project);
		return preferences.getBoolean(key, false);
	}

	private static IEclipsePreferences getProjectPreferences(IProject project) {
		IScopeContext projectContext = new ProjectScope(project);
		return projectContext.getNode(NescPlugin.PLUGIN_ID);
	}

	private static String listToString(List<String> list) {
		final int size = list.size();
		if (size == 0) {
			return "";
		}
		final StringBuilder builder = new StringBuilder();

		builder.append(list.get(0));
		for (int i = 1; i < size; ++i) {
			builder.append(',');
			builder.append(list.get(i));
		}
		return builder.toString();
	}

	private static List<String> stringToList(String str) {
		if (str == null || str.isEmpty()) {
			return Collections.emptyList();
		}
		final String[] parts = str.split(",");
		final List<String> result = new ArrayList<>(parts.length);
		for (String part : parts) {
			result.add(part);
		}
		return result;
	}
}
