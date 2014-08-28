package pl.edu.mimuw.nesc.plugin.frontend;

import static pl.edu.mimuw.nesc.plugin.projects.util.EnvironmentVariableResolver.resolveTosDirVariable;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import pl.edu.mimuw.nesc.ContextRef;
import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.Frontend;
import pl.edu.mimuw.nesc.ProjectData;
import pl.edu.mimuw.nesc.exception.InvalidOptionsException;
import pl.edu.mimuw.nesc.plugin.NescPlugin;
import pl.edu.mimuw.nesc.plugin.marker.MarkerHelper;
import pl.edu.mimuw.nesc.plugin.projects.util.NescPlatformUtil;
import pl.edu.mimuw.nesc.plugin.projects.util.NescPlatformUtil.NescPlatform;
import pl.edu.mimuw.nesc.plugin.resources.PathsUtil;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Project manager is responsible for creating, deleting projects, persisting
 * and updating its data.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 *
 */
public final class FrontendManager {

	private static final Object LOCK = new Object();
	private static final Map<String, ProjectCache> PROJECTS = new HashMap<String, ProjectCache>();

	/**
	 * Ensures that a frontend context for the project exists. If not, it is
	 * created.
	 *
	 * @param project
	 *            project
	 *
	 * @return <code>Optional.absent()</code> if project context was created
	 *         successfully, otherwise an error message will be returned
	 */
	public static Optional<String> ensureContext(IProject project) {
		Preconditions.checkNotNull(project, "project cannot be null");
		synchronized (LOCK) {
			final Optional<ProjectCache> cache = getProjectCache(project);
			if (cache.isPresent()) {
				return Optional.<String> absent();
			}
			try {
				final String options[] = getProjectArgs(project);
				final ContextRef context = getFrontend().createContext(options);
				PROJECTS.put(project.getName(), new ProjectCache(context));
			} catch (InvalidOptionsException e) {
				e.printStackTrace();
				return Optional.of(e.getMessage());
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

	/**
	 * Updates options of the context associated with given project.
	 *
	 * @param project
	 *            project
	 */
	public static void updateContext(IProject project) {
		Preconditions.checkNotNull(project, "project cannot be null");
		synchronized (LOCK) {
			ensureContext(project);
			final ContextRef context = getProjectCache(project).get().getContextRef();
			try {
				getFrontend().updateSettings(context, getProjectArgs(project));
			} catch (ConfigurationException | InvalidOptionsException | IOException e) {
				// TODO should not happen! but show error dialog?
				e.printStackTrace();
			}
		}
	}

	/**
	 * Deletes the context associated with given project.
	 *
	 * @param projectName
	 *            project name
	 */
	public static void deleteContext(String projectName) {
		Preconditions.checkNotNull(projectName, "project name be null");
		synchronized (LOCK) {
			final ProjectCache cache = PROJECTS.get(projectName);
			if (cache == null) {
				// TODO
				return;
			}
			getFrontend().deleteContext(cache.getContextRef());
			PROJECTS.remove(projectName);
		}
	}

	/**
	 * (Re)builds given project.
	 *
	 * @param project
	 *            project
	 * @return project data
	 */
	public static ProjectData buildContext(IProject project) {
		Preconditions.checkNotNull(project, "project cannot be null");
		synchronized (LOCK) {
			ensureContext(project);
		}

		// Long-running operations should not be in synchronized blocks.
		final ContextRef context = getProjectCache(project).get().getContextRef();
		final ProjectData projectData = getFrontend().build(context);

		synchronized (LOCK) {
			final ProjectCache cache = getProjectCache(project).get();
			cache.setProjectData(projectData);
			for (FileData data : projectData.getFileDatas().values()) {
				cache.getFilesMap().put(data.getFilePath(), data);
			}
		}
		MarkerHelper.updateMarkersJob(project);
		return projectData;
	}

	/**
	 * Updates specified file in given context.
	 *
	 * @param project
	 *            project
	 * @param filePath
	 *            file path
	 * @return data of specified file and its newly parsed dependencies
	 */
	public static ProjectData updateFile(IProject project, String filePath) {
		synchronized (LOCK) {
			ensureContext(project);
		}

		// Long-running operations should not be in synchronized blocks.
		final ContextRef context = getProjectCache(project).get().getContextRef();
		final ProjectData projectData = getFrontend().update(context, filePath);

		synchronized (LOCK) {
			final ProjectCache cache = getProjectCache(project).get();
			for (FileData data : projectData.getFileDatas().values()) {
				cache.getFilesMap().put(data.getFilePath(), data);
			}
			return projectData;
		}
	}

	public static ProjectData getProjectData(IProject project) {
		synchronized (LOCK) {
			final ProjectData projectData = PROJECTS.get(project.getName()).getProjectData();
			if (projectData == null) {
				// TODO should not happen!
			}
			return projectData;
		}
	}

	public static Optional<FileData> getFileData(IProject project, String filePath) {
		synchronized (LOCK) {
			if (!PROJECTS.containsKey(project.getName())) {
				// TODO should not happen!
			}
			return Optional.fromNullable(PROJECTS.get(project.getName()).getFilesMap().get(filePath));
		}
	}

	private static Optional<ProjectCache> getProjectCache(IProject project) {
		synchronized (LOCK) {
			return Optional.fromNullable(PROJECTS.get(project.getName()));
		}
	}

	private static Frontend getFrontend() {
		return NescPlugin.getDefault().getNescFrontend();
	}

	private static String[] getProjectArgs(IProject project) throws ConfigurationException, IOException {
		final IPath projectPath = project.getLocation();

		final List<String> args = new ArrayList<>();

		final String mainConfiguration = getProjectPreferenceValue(project, MAIN_CONFIGURATION);
		final boolean isTinyOsProject = getProjectPreferenceValueB(project, TINYOS_PROJECT);

		final List<String> additionalPaths;
		final List<String> addtionalDefaultFiles;
		final List<String> additionalMacros = getProjectPreferenceValueStringList(project, ADDITIONAL_PREDEFINED_MACROS);
		final NescPlatform platform;

		if (isTinyOsProject) {
			final String platformName = getProjectPreferenceValue(project, TINYOS_PLATFORM);
			final boolean isPlatformPredefined = getProjectPreferenceValueB(project, TINYOS_PREDEFINED_PLATFORM);
			final String tinyOsPath = getProjectPreferenceValue(project, TINYOS_PATH);
			platform = NescPlatformUtil.loadPlatformProperties(platformName, isPlatformPredefined, tinyOsPath);
			addtionalDefaultFiles = resolveTosDirVariable(
					getProjectPreferenceValueStringList(project, ADDITIONAL_DEFAULT_FILES), tinyOsPath);
			additionalPaths = PathsUtil.getResolvedNonPlatformPaths(project, Optional.of(tinyOsPath));
		} else {
			addtionalDefaultFiles = getProjectPreferenceValueStringList(project, ADDITIONAL_DEFAULT_FILES);
			platform = NescPlatformUtil.getDummyPlatform();
			additionalPaths = PathsUtil.getResolvedNonPlatformPaths(project, Optional.<String> absent());
		}

		addOption(args, "-include", platform.getFiles(), addtionalDefaultFiles);
		addOption(args, "-I", platform.getPaths(), additionalPaths);
		addOption(args, "-D", platform.getMacros(), additionalMacros);
		addOption(args, "-m", mainConfiguration);
		addOption(args, "-p", projectPath.toOSString());

		return args.toArray(new String[args.size()]);
	}

	private static void addOption(List<String> options, String key, String[] platformValues, List<String> userValues) {
		if (platformValues.length == 0 && userValues.isEmpty()) {
			return;
		}

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
