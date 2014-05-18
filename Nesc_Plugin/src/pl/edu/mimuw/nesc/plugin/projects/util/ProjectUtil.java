package pl.edu.mimuw.nesc.plugin.projects.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.ContextRef;
import pl.edu.mimuw.nesc.ProjectData;
import pl.edu.mimuw.nesc.exception.InvalidOptionsException;
import pl.edu.mimuw.nesc.plugin.NescPlugin;

public class ProjectUtil {
	static private IEclipsePreferences getProjectPreferences(IProject project) {
		IScopeContext projectContext = new ProjectScope(project);
		return projectContext.getNode(NescPlugin.PLUGIN_ID);
	}

	static public void setProjectPreferenceValue(IProject project, String key, String value) throws BackingStoreException {
		IEclipsePreferences preferences = getProjectPreferences(project);
		preferences.put(key, value);
		preferences.flush();
	}

	static public void setProjectPreferenceValue(IProject project, String key, Integer value) throws BackingStoreException {
		IEclipsePreferences preferences = getProjectPreferences(project);
		preferences.putInt(key, value);
		preferences.flush();
	}

	static public void setProjectPreferenceValue(IProject project, String key, Boolean value) throws BackingStoreException {
		IEclipsePreferences preferences = getProjectPreferences(project);
		preferences.putBoolean(key, value);
		preferences.flush();
	}

	static public String getProjectPreferenceValue(IProject project, String key) {
		IEclipsePreferences preferences = getProjectPreferences(project);
		return preferences.get(key, "");
	}

	static public Integer getProjectPreferenceValueI(IProject project, String key) {
		IEclipsePreferences preferences = getProjectPreferences(project);
		return preferences.getInt(key, -1);
	}

	static public Boolean getProjectPreferenceValueB(IProject project, String key) {
		IEclipsePreferences preferences = getProjectPreferences(project);
		return preferences.getBoolean(key, false);
	}

	 static public ContextRef getProjectContext(IProject project) {
		if (project == null) {
			return null;
		}
		return NescPlugin.getDefault().getProjectContext(project.getName());
	}

	static public ProjectData getProjectData(IProject project) {
		if (project == null) {
			return null;
		}
		return NescPlugin.getDefault().getProjectData(project.getName());
	}

	static public boolean ensureContext(IProject project) {
		if (project == null) {
			return false;
		}
		if (getProjectContext(project) == null) {
			IPath projectPath = project.getLocation();
			String mainConfiguration = getProjectPreferenceValue(project, NescProjectPreferences.MAIN_CONFIGURATION);

			String options[] = { "-m", mainConfiguration, "-p", projectPath.toOSString() };

			try {
				ContextRef context = NescPlugin.getDefault().getNescFrontend().createContext(options);
				NescPlugin.getDefault().setProjectContext(project.getName(), context);
			} catch (InvalidOptionsException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static ProjectData rebuildProjectContext(IProject project) {
		ContextRef projectContext = getProjectContext(project);
		if (projectContext == null) {
			return null;
		}
		ProjectData data = NescPlugin.getDefault().getNescFrontend().rebuild(projectContext);
		NescPlugin.getDefault().setProjectData(project.getName(), data);
		return data;
	}
}
