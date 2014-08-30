package pl.edu.mimuw.nesc.plugin.preferences;

import static pl.edu.mimuw.nesc.plugin.preferences.NescPluginPreferences.*;

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

	private static final String PLATFORMS_DIRECTORY = "Platforms directory:";
	private static final String OS_DIRECTORY = "OS directory:";
	private static final String NCLIB_DIRECTORY = "NesC lib directory:";
	private static final String CLIB_DIRECTORY = "clib directory:";
	private static final String HWLIB_DIRECTORY = "hwlib directory:";

	private DirectorySelector platformsLocSelector;
	private DirectorySelector osLocSelector;
	private DirectorySelector nclibLocSelector;
	private DirectorySelector clibLocSelector;
	private DirectorySelector hwlibLocSelector;

	@Override
	protected Control createContents(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);
		container.setLayoutData(parentData);

		final Label platformLabel = new Label(container, SWT.NONE);
		platformLabel.setText(PLATFORMS_DIRECTORY);
		platformsLocSelector = new DirectorySelector(container);

		final Label osLabel = new Label(container, SWT.NONE);
		osLabel.setText(OS_DIRECTORY);
		osLocSelector = new DirectorySelector(container);

		final Label nclibLabel = new Label(container, SWT.NONE);
		nclibLabel.setText(NCLIB_DIRECTORY);
		nclibLocSelector = new DirectorySelector(container);

		final Label clibLabel = new Label(container, SWT.NONE);
		clibLabel.setText(CLIB_DIRECTORY);
		clibLocSelector = new DirectorySelector(container);

		final Label hwlibLabel = new Label(container, SWT.NONE);
		hwlibLabel.setText(HWLIB_DIRECTORY);
		hwlibLocSelector = new DirectorySelector(container);

		initializeValues();
		return new Composite(parent, SWT.NULL);
	}

	@Override
	protected void initializeDefaults() {
		final IPreferenceStore store = getPreferenceStore();
		platformsLocSelector.setPath(store.getDefaultString(PLATFORMS_DIR));
		osLocSelector.setPath(store.getDefaultString(OS_LOC));
		nclibLocSelector.setPath(store.getDefaultString(NCLIB_LOC));
		clibLocSelector.setPath(store.getDefaultString(CLIB_LOC));
		hwlibLocSelector.setPath(store.getDefaultString(HWLIB_LOC));
	}

	@Override
	protected void initializeValues() {
		final IPreferenceStore store = getPreferenceStore();
		platformsLocSelector.setPath(store.getString(PLATFORMS_DIR));
		osLocSelector.setPath(store.getString(OS_LOC));
		nclibLocSelector.setPath(store.getString(NCLIB_LOC));
		clibLocSelector.setPath(store.getString(CLIB_LOC));
		hwlibLocSelector.setPath(store.getString(HWLIB_LOC));
	}

	@Override
	protected void storeValues() {
		final IPreferenceStore store = getPreferenceStore();
		store.setValue(PLATFORMS_DIR, platformsLocSelector.getSelectedPath());
		store.setValue(OS_LOC, osLocSelector.getSelectedPath());
		store.setValue(NCLIB_LOC, nclibLocSelector.getSelectedPath());
		store.setValue(CLIB_LOC, clibLocSelector.getSelectedPath());
		store.setValue(HWLIB_LOC, hwlibLocSelector.getSelectedPath());
	}
}
