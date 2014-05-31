package pl.edu.mimuw.nesc.plugin.projects.util;

import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_DEFAULT_FILES;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_INCLUDE_PATHS;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_PREDEFINED_MACROS;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.MAIN_CONFIGURATION;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PATH;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PLATFORM;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PREDEFINED_PLATFORM;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.getProjectPreferenceValue;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.getProjectPreferenceValueB;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.getProjectPreferenceValueStringList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import pl.edu.mimuw.nesc.ContextRef;
import pl.edu.mimuw.nesc.ProjectData;
import pl.edu.mimuw.nesc.exception.InvalidOptionsException;
import pl.edu.mimuw.nesc.plugin.NescPlugin;
import pl.edu.mimuw.nesc.plugin.projects.util.NescPlatformUtil.NescPlatform;

import com.google.common.base.Optional;

/**
 *
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class ProjectUtil {

	public static ContextRef getProjectContext(IProject project) {
		if (project == null) {
			return null;
		}
		return NescPlugin.getDefault().getProjectContext(project.getName());
	}

	public static ProjectData getProjectData(IProject project) {
		if (project == null) {
			return null;
		}
		return NescPlugin.getDefault().getProjectData(project.getName());
	}


	/**
	 * Tries to create project context.
	 *
	 * @param project
	 *            project
	 * @return <code>Optional.absent()</code> if project context was created successfully,
	 *         otherwise an error message will be returned.
	 */
	public static Optional<String> ensureContext(IProject project) {
		if (project == null) {
			return Optional.<String>absent();
		}
		if (getProjectContext(project) == null) {
			try {
				final String options[] = getProjectArgs(project);
				final ContextRef context = NescPlugin.getDefault().getNescFrontend().createContext(options);
				NescPlugin.getDefault().setProjectContext(project.getName(), context);
			} catch (InvalidOptionsException e) {
				e.printStackTrace();
				return Optional.of(e.getMessage());
			} catch (FileNotFoundException e) {
				/* Could not find main file. */
				e.printStackTrace();
				/*
				 * When a new project is created, the main configuration does
				 * not exist yet.
				 */
				return Optional.<String>absent();
			} catch (ConfigurationException | IOException e) {
				e.printStackTrace();
				return Optional.of(e.getMessage());
			} catch (Exception e) {
				/* Unexpected error. */
				e.printStackTrace();
				return Optional.of(e.getMessage());
			}
		}
		return Optional.<String>absent();
	}

	public static ProjectData rebuildProjectContext(IProject project) {
		ContextRef projectContext = getProjectContext(project);
		if (projectContext == null) {
			return null;
		}
		try {
		ProjectData data = NescPlugin.getDefault().getNescFrontend().rebuild(projectContext);
		NescPlugin.getDefault().setProjectData(project.getName(), data);
		return data;
		} catch (FileNotFoundException e) {
			/* Main file was not found. */
			return null;
		}
	}

	private static String[] getProjectArgs(IProject project) throws ConfigurationException, IOException {
		final IPath projectPath = project.getLocation();

		final String mainConfiguration = getProjectPreferenceValue(project, MAIN_CONFIGURATION);
		final String platformName = getProjectPreferenceValue(project, TINYOS_PLATFORM);
		final boolean isPlatformPredefined = getProjectPreferenceValueB(project, TINYOS_PREDEFINED_PLATFORM);
		final String tinyOsPath = getProjectPreferenceValue(project, TINYOS_PATH);

		final NescPlatform platform = NescPlatformUtil.loadPlatformProperties(platformName, isPlatformPredefined,
				tinyOsPath);
		final List<String> additionalPaths = getProjectPreferenceValueStringList(project, ADDITIONAL_INCLUDE_PATHS);
		final List<String> addtionalDefaultFiles = getProjectPreferenceValueStringList(project,
				ADDITIONAL_DEFAULT_FILES);
		final List<String> additionalMacros = getProjectPreferenceValueStringList(project, ADDITIONAL_PREDEFINED_MACROS);

		List<String> args = new ArrayList<>();

		addOption(args, "-include", platform.getFiles(), addtionalDefaultFiles);
		addOption(args, "-I", platform.getPaths(), additionalPaths);
		addOption(args, "-D", platform.getMacros(), additionalMacros);
		addOption(args, "-m", mainConfiguration);
		addOption(args, "-p", projectPath.toOSString());

		return args.toArray(new String[args.size()]);
	}

	private static void addOption(List<String> options, String key, String[] platformValues, List<String> userValues) {
		options.add(key);
		for (String value : platformValues) {
			// FIXME: empty strings in additional*
			if (!value.isEmpty()) {
				options.add(value);
			}
		}
		for (String value : userValues) {
			if (!value.isEmpty()) {
				options.add(value);
			}
		}
	}

	private static void addOption(List<String> options, String key, String value) {
		options.add(key);
		options.add(value);
	}
}
