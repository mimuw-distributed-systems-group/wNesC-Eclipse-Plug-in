package pl.edu.mimuw.nesc.plugin.projects;

import static org.eclipse.swt.SWT.FILL;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_DEFAULT_FILES;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_INCLUDE_PATHS;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_PREDEFINED_MACROS;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.getProjectPreferenceValueStringList;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.setProjectPreferenceValue;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.wizards.composite.ProjectAdditionalPathSettingsComposite;

/**
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 */
public class NescProjectAdditionalPathSettingsPage extends NescPropertyPage {

	private ProjectAdditionalPathSettingsComposite composite;

	@Override
	protected Control createContents(Composite parent) {
		IProject project = getProject();
		if (project == null) {
			return parent;
		}
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		GridData parentData = new GridData(FILL, FILL, true, true);
		container.setLayout(layout);
		container.setLayoutData(parentData);

		composite = new ProjectAdditionalPathSettingsComposite(container, listener);
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
		composite.setData(getProjectPreferenceValueStringList(project, ADDITIONAL_INCLUDE_PATHS),
				getProjectPreferenceValueStringList(project, ADDITIONAL_DEFAULT_FILES),
				getProjectPreferenceValueStringList(project, ADDITIONAL_PREDEFINED_MACROS));
	}

	@Override
	protected void storeValues() {
		/*
		 * TODO: Recreate context when settings are changed. (use
		 * IEclipsePreferences.(Node|Preference)ChangeEvent?)
		 */
		final IProject project = getProject();
		if (project == null) {
			System.err.println("No project found!");
			return;
		}
		try {
			setProjectPreferenceValue(project, ADDITIONAL_DEFAULT_FILES, composite.getAdditionalDefaultIncludes());
			setProjectPreferenceValue(project, ADDITIONAL_INCLUDE_PATHS, composite.getAdditionalIncludePaths());
			setProjectPreferenceValue(project, ADDITIONAL_PREDEFINED_MACROS, composite.getAdditionalPredefinedMacros());
			this.setErrorMessage(null);
		} catch (BackingStoreException e) {
			this.setErrorMessage("Failed to save changes to project properties");
		}
	}

}
