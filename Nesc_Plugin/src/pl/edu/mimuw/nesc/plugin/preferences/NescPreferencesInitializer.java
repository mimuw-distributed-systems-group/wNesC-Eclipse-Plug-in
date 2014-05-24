package pl.edu.mimuw.nesc.plugin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import pl.edu.mimuw.nesc.plugin.NescPlugin;

/**
 * Saves in preference store default values. Called when user executes
 * "Restore Defaults" or preference page is opened for the first time.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescPreferencesInitializer extends AbstractPreferenceInitializer {

	/*
	 * http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fextension-points%2Forg_eclipse_core_runtime_preferences.html
	 */

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = NescPlugin.getDefault().getPreferenceStore();
		store.setDefault(NescPluginPreferences.PLATFORMS_DIR, ""); //$NON-NLS-1$
	}

}
