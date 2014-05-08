package pl.edu.mimuw.nesc.plugin.projects.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

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
}
