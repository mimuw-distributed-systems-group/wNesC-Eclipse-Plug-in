package pl.edu.mimuw.nesc.plugin.projects.util;

import static pl.edu.mimuw.nesc.plugin.projects.util.EnvironmentVariableResolver.*;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import pl.edu.mimuw.nesc.ContextRef;
import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.ProjectData;
import pl.edu.mimuw.nesc.exception.InvalidOptionsException;
import pl.edu.mimuw.nesc.plugin.NescPlugin;
import pl.edu.mimuw.nesc.plugin.projects.util.NescPlatformUtil.NescPlatform;

import com.google.common.base.Optional;

/**
 * Project manager is responsible for creating, deleting projects, persisting
 * and updating its data.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 *
 */
public final class ProjectManager {

	/**
	 * Map of all opened projects (keys are project names).
	 */
	private static final ConcurrentMap<String, ProjectCache> PROJECT_DATA_MAP = new ConcurrentHashMap<String, ProjectCache>();

	/**
	 * Gets project cache (containing all data associated with project).
	 *
	 * @param project
	 *            project
	 * @return project data
	 */
	public static ProjectCache getProjectCache(IProject project) {
		if (project == null) {
			return null;
		}
		return PROJECT_DATA_MAP.get(project.getName());
	}

	/**
	 * Gets project's NesC frontend context
	 *
	 * @param project
	 *            project
	 * @return NesC frontendContext
	 */
	public static ContextRef getProjectContext(IProject project) {
		if (project == null) {
			return null;
		}
		final ProjectCache cache = PROJECT_DATA_MAP.get(project.getName());
		if (cache == null) {
			return null;
		}
		return cache.getContextRef();
	}

	/**
	 * Gets project's data returned by NesC frontend.
	 *
	 * @param project
	 *            project
	 * @return data returned by NesC frontend
	 */
	public static ProjectData getProjectData(IProject project) {
		if (project == null) {
			return null;
		}
		final ProjectCache cache = PROJECT_DATA_MAP.get(project.getName());
		if (cache == null) {
			return null;
		}
		return cache.getProjectData();
	}

	/**
	 * <p>
	 * Ensures that a NesC frontend context exists. If not the context will be
	 * created and the entire project will be build.
	 * </p>
	 *
	 * <p>
	 * NOTE: if context already exists, rebuild will not be performed.
	 * </p>
	 *
	 * @param project
	 *            project
	 * @return <code>Optional.absent()</code> if project context was created
	 *         successfully, otherwise an error message will be returned
	 */
	public static Optional<String> ensureContextWithRebuild(IProject project) {
		return ensureContext(project, true);
	}

	/**
	 * Tries to create project context without building project.
	 *
	 * @param project
	 *            project
	 * @return <code>Optional.absent()</code> if project context was created
	 *         successfully, otherwise an error message will be returned
	 */
	public static Optional<String> ensureContext(IProject project) {
		return ensureContext(project, false);
	}

	/**
	 * Rebuild given project.
	 *
	 * @param project
	 *            project
	 * @return project data (from NesC frontend)
	 */
	public static ProjectData rebuildProjectContext(IProject project) {
		ContextRef projectContext = getProjectContext(project);
		if (projectContext == null) {
			return null;
		}
		try {
			ProjectData data = NescPlugin.getDefault().getNescFrontend().rebuild(projectContext);
			setProjectData(project, data);
			return data;
		} catch (FileNotFoundException e) {
			/* Main file was not found. */
			return null;
		}
	}

	/**
	 * Updates specified file in given project's context.
	 *
	 * @param project
	 *            project
	 * @param filePath
	 *            file path
	 */
	public static void updateFile(IProject project, String filePath) {
		ensureContextWithRebuild(project);
		final FileData data = NescPlugin.getDefault().getNescFrontend().update(getProjectContext(project), filePath);
		PROJECT_DATA_MAP.get(project.getName()).getFilesMap().put(filePath, data);
	}

	/**
	 * Gets file's data.
	 *
	 * @param project
	 *            project
	 * @param filePath
	 *            file path
	 * @return file's data
	 */
	public static FileData getFileData(IProject project, String filePath) {
		final ProjectCache cache = getProjectCache(project);
		if (cache == null) {
			return null;
		}
		return cache.getFilesMap().get(filePath);
	}

	/**
	 * Deletes all data associated with project of given name.
	 *
	 * @param name
	 *            project's name
	 */
	public static void deleteProject(String name) {
		PROJECT_DATA_MAP.remove(name);
	}

	private static void createProjectCache(IProject project, ContextRef contextRef) {
		final ProjectCache cache = new ProjectCache(contextRef);
		PROJECT_DATA_MAP.put(project.getName(), cache);
	}

	private static void setProjectData(IProject project, ProjectData projectData) {
		final ProjectCache cache = PROJECT_DATA_MAP.get(project.getName());
		if (cache == null) {
			// TODO
			return;
		}
		cache.setProjectData(projectData);
	}

	/**
	 * Tries to create project context.
	 *
	 * @param project
	 *            project
	 * @param doRebuild
	 *            indicates if project should be rebuild after creating context
	 *            (if it had not existed before)
	 * @return <code>Optional.absent()</code> if project context was created
	 *         successfully, otherwise an error message will be returned.
	 */
	private static Optional<String> ensureContext(IProject project, boolean doRebuild) {
		if (project == null) {
			return Optional.<String> absent();
		}

		if (getProjectCache(project) == null) {
			try {
				final String options[] = getProjectArgs(project);
				final ContextRef context = NescPlugin.getDefault().getNescFrontend().createContext(options);
				createProjectCache(project, context);
				if (doRebuild) {
					rebuildProjectContext(project);
				}
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
				return Optional.<String> absent();
			} catch (ConfigurationException | IOException e) {
				e.printStackTrace();
				return Optional.of(e.getMessage());
			} catch (Exception e) {
				/* Unexpected error. */
				e.printStackTrace();
				return Optional.of(e.getMessage());
			}
		}
		return Optional.<String> absent();
	}

	private static String[] getProjectArgs(IProject project) throws ConfigurationException, IOException {
		final IPath projectPath = project.getLocation();

		final String mainConfiguration = getProjectPreferenceValue(project, MAIN_CONFIGURATION);
		final String platformName = getProjectPreferenceValue(project, TINYOS_PLATFORM);
		final boolean isPlatformPredefined = getProjectPreferenceValueB(project, TINYOS_PREDEFINED_PLATFORM);
		final String tinyOsPath = getProjectPreferenceValue(project, TINYOS_PATH);

		final NescPlatform platform = NescPlatformUtil.loadPlatformProperties(platformName, isPlatformPredefined,
				tinyOsPath);
		final List<String> additionalPaths = resolveTosDirVariable(
				getProjectPreferenceValueStringList(project, ADDITIONAL_INCLUDE_PATHS), tinyOsPath);
		final List<String> addtionalDefaultFiles = resolveTosDirVariable(
				getProjectPreferenceValueStringList(project, ADDITIONAL_DEFAULT_FILES), tinyOsPath);
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
