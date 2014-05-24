package pl.edu.mimuw.nesc.plugin.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import pl.edu.mimuw.nesc.plugin.NescPlugin;

/**
 * Base class for plugin preference pages.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public abstract class NescPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/*
	 * http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fpreferences_prefs_contribute.htm
	 */

	@Override
	public void init(IWorkbench workbench) {
		// nothing to do
	}

	@Override
	public void performDefaults() {
		initializeDefaults();
	}

	@Override
	public boolean performOk() {
		storeValues();
		return true;
	}

	/**
	 * Restores default preferences (e.g. when user presses <b>Restore
	 * Defaults</b>.
	 */
	protected abstract void initializeDefaults();

	/**
	 * Initializes all controls with current preference values.
	 */
	protected abstract void initializeValues();

	/**
	 * Saves the current values of the controls on the preference page into
	 * preferences store.
	 */
	protected abstract void storeValues();

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return NescPlugin.getDefault().getPreferenceStore();
	}

}
