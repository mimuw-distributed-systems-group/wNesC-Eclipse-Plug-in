package pl.edu.mimuw.nesc.plugin.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

import pl.edu.mimuw.nesc.plugin.NescPlugin;

/**
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class NescPluginPreferences {

	/*
	 * Useful links:
	 * http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fruntime_preferences.htm
	 */

	public static final String PLATFORMS_DIR = "pl.edu.mimuw.nesc.plugin.platforms_loc";
	public static final String OS_LOC = "pl.edu.mimuw.nesc.plugin.os_loc";
	public static final String NCLIB_LOC = "pl.edu.mimuw.nesc.plugin.nclib_loc";
	public static final String CLIB_LOC = "pl.edu.mimuw.nesc.plugin.clib_loc";
	public static final String HWLIB_LOC = "pl.edu.mimuw.nesc.plugin.hwlib_loc";
	public static final String HEAD_COMMENT = "pl.edu.mimuw.nesc.plugin.head_comment";
	public static final String ENTITY_COMMENT = "pl.edu.mimuw.nesc.plugin.entity_comment";

	public static String getString(String key) {
		return getStore().getString(key);
	}

	private static IPreferenceStore getStore() {
		return NescPlugin.getDefault().getPreferenceStore();
	}

}
