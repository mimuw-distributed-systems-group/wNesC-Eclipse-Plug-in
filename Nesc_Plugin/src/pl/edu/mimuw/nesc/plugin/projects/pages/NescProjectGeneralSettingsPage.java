package pl.edu.mimuw.nesc.plugin.projects.pages;

import static org.eclipse.swt.SWT.FILL;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.*;
import static pl.edu.mimuw.nesc.plugin.preferences.NescPluginPreferences.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.preferences.NescPluginPreferences;
import pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences;
import pl.edu.mimuw.nesc.plugin.variable.PathVariable;
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
		composite.setData(getProjectPreferenceValue(project, MAIN_CONFIGURATION),
				getProjectPreferenceValue(project, TINYOS_PLATFORM),
				getProjectPreferenceValueB(project, TINYOS_PREDEFINED_PLATFORM),
				getValueString(project, TINYOS_PATH, OS_LOC),
				getValueString(project, NCLIB_PATH, NCLIB_LOC),
				getValueString(project, CLIB_PATH, CLIB_LOC),
				getValueString(project, HWLIB_PATH, HWLIB_LOC));
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
					.set(MAIN_CONFIGURATION, composite.getMainConfiguration())
					.set(TINYOS_PLATFORM, composite.getTinyOsPlatform())
					.set(TINYOS_PREDEFINED_PLATFORM, composite.isPlatformPredefined())
					.set(TINYOS_PATH, composite.getTinyOsPath())
					.set(NCLIB_PATH, composite.getNescLibPath())
					.set(CLIB_PATH, composite.getClibPath())
					.set(HWLIB_PATH, composite.getHwlibPath())
					.commit();

			final IPathVariableManager pathManager = project.getPathVariableManager();
			pathManager.setURIValue(PathVariable.OSDIR_NAME, new URI(composite.getTinyOsPath()));
			pathManager.setURIValue(PathVariable.NCLIBDIR_NAME, new URI(composite.getNescLibPath()));
			pathManager.setURIValue(PathVariable.CLIBDIR_NAME, new URI(composite.getClibPath()));
			pathManager.setURIValue(PathVariable.HWLIBDIR_NAME, new URI(composite.getHwlibPath()));

			this.setErrorMessage(null);
		} catch (BackingStoreException e) {
			this.setErrorMessage("Failed to save changes to project properties");
		} catch (CoreException e) {
			this.setErrorMessage("Failed to save changes to project properties; core exception");
			e.printStackTrace();
		} catch (URISyntaxException e) {
			this.setErrorMessage("Failed to save changes to project properties; invalid path");
			e.printStackTrace();
		}
	}

	private String getValueString(IProject project, String projectProperty, String pluginProperty) {
		String value = getProjectPreferenceValue(project, projectProperty);
		if (value != null && !value.isEmpty()) {
			return value;
		}
		return NescPluginPreferences.getString(pluginProperty);
	}
}
