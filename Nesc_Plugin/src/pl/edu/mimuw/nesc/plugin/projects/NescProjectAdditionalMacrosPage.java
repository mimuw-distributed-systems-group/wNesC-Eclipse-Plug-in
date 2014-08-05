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

import pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences;
import pl.edu.mimuw.nesc.plugin.wizards.composite.ProjectAdditionalMacrosComposite;

/**
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 */
public class NescProjectAdditionalMacrosPage extends NescPropertyPage {

	private ProjectAdditionalMacrosComposite composite;

	@Override
	protected Control createContents(Composite parent) {
		final IProject project = getProject();
		if (project == null) {
			return parent;
		}

		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		GridData layoutData = new GridData(FILL, FILL, true, true);
		container.setLayout(layout);
		container.setLayoutData(layoutData);

		composite = new ProjectAdditionalMacrosComposite(container, compositeListener);
		initializeValues();
		return container;
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
		composite.setData(getProjectPreferenceValueStringList(project, ADDITIONAL_DEFAULT_FILES),
				getProjectPreferenceValueStringList(project, ADDITIONAL_PREDEFINED_MACROS));
	}

	@Override
	protected void storeValues() {
		final IProject project = getProject();
		if (project == null) {
			System.err.println("No project found!");
			return;
		}
		try {
			NescProjectPreferences.transaction(project)
					.set(ADDITIONAL_DEFAULT_FILES, composite.getAdditionalDefaultIncludes())
					.set(ADDITIONAL_PREDEFINED_MACROS, composite.getAdditionalPredefinedMacros())
					.commit();
			this.setErrorMessage(null);
		} catch (BackingStoreException e) {
			this.setErrorMessage("Failed to save changes to project properties");
		}
	}
}
