package pl.edu.mimuw.nesc.plugin.preferences;

import static pl.edu.mimuw.nesc.plugin.preferences.NescPluginPreferences.PLATFORMS_DIR;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import pl.edu.mimuw.nesc.plugin.wizards.composite.DirectorySelector;

/**
 * Plugin preference main page.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescPluginMainPreferencesPage extends NescPreferencePage {

	private static final String PLATFORMS_DIRECTORY = "Platforms directory:"; //$NON-NLS-1$

	private DirectorySelector platformsDirSelector;

	@Override
	protected Control createContents(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);
		container.setLayoutData(parentData);

		final Label label = new Label(container, SWT.NONE);
		label.setText(PLATFORMS_DIRECTORY);

		platformsDirSelector = new DirectorySelector(container);
		initializeValues();
		return new Composite(parent, SWT.NULL);
	}

	@Override
	protected void initializeDefaults() {
		final IPreferenceStore store = getPreferenceStore();
		platformsDirSelector.setPath(store.getDefaultString(PLATFORMS_DIR));
	}

	@Override
	protected void initializeValues() {
		final IPreferenceStore store = getPreferenceStore();
		platformsDirSelector.setPath(store.getString(PLATFORMS_DIR));
	}

	@Override
	protected void storeValues() {
		final IPreferenceStore store = getPreferenceStore();
		store.setValue(PLATFORMS_DIR, platformsDirSelector.getSelectedPath());
	}
}
