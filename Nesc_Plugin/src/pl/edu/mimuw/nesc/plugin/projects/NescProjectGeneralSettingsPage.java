package pl.edu.mimuw.nesc.plugin.projects;

import static org.eclipse.swt.SWT.FILL;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.wizards.composite.ProjectGeneralSettingsComposite;

/**
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 */
public class NescProjectGeneralSettingsPage extends NescPropertyPage {

	private ProjectGeneralSettingsComposite composite;

	@Override
	protected Control createContents(Composite parent) {
		final IProject project = getProject();
		if (project == null) {
			return parent;
		}
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		GridData parentData = new GridData(FILL, FILL, true, true);
		container.setLayout(layout);
		container.setLayoutData(parentData);

		composite = new ProjectGeneralSettingsComposite(container, compositeListener);
		initializeValues();
		return new Composite(parent, SWT.NULL);
	}

	@Override
	protected void initializeDefaults() {
		// TODO
		initializeValues();
	}

	@Override
	protected void initializeValues() {
		final IProject project = getProject();
		if (project == null) {
			System.err.println("No project found!");
			return;
		}
		composite.setData(getProjectPreferenceValue(project, MAIN_CONFIGURATION),
				getProjectPreferenceValueB(project, TINYOS_PROJECT),
				getProjectPreferenceValue(project, TINYOS_PLATFORM),
				getProjectPreferenceValueB(project, TINYOS_PREDEFINED_PLATFORM),
				getProjectPreferenceValue(project, TINYOS_PATH));
	}

	@Override
	protected void storeValues() {
		final IProject project = getProject();
		if (project == null) {
			System.err.println("No project found!");
			return;
		}
		try {
			setProjectPreferenceValue(project, MAIN_CONFIGURATION, composite.getMainConfiguration());
			setProjectPreferenceValue(project, TINYOS_PROJECT, composite.isTinyOsProject());
			setProjectPreferenceValue(project, TINYOS_PLATFORM, composite.getTinyOsPlatform());
			setProjectPreferenceValue(project, TINYOS_PREDEFINED_PLATFORM, composite.isPlatformPredefined());
			setProjectPreferenceValue(project, TINYOS_PATH, composite.getTinyOsPath());
			this.setErrorMessage(null);
		} catch (BackingStoreException e) {
			this.setErrorMessage("Failed to save changes to project properties");
		}
	}
}
