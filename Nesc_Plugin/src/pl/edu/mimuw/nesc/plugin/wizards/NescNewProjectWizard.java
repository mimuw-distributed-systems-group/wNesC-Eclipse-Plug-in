package pl.edu.mimuw.nesc.plugin.wizards;

import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_DEFAULT_FILES;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_INCLUDE_PATHS;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_PREDEFINED_MACROS;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.MAIN_CONFIGURATION;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PATH;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PLATFORM;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PREDEFINED_PLATFORM;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PROJECT;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.setProjectPreferenceValue;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.projects.NescProjectSupport;
import pl.edu.mimuw.nesc.plugin.projects.util.ProjectManager;

import com.google.common.base.Optional;

/**
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *         <p>
 *         Additional project properties.
 *         </p>
 *
 */
public class NescNewProjectWizard extends Wizard implements INewWizard {
	/**
	 * Identifier of the wizard from 'plugin.xml' file.
	 */
	public static final String WIZARD_ID = "pl.edu.mimuw.nesc.wizards.new.NescProjectWizard";

	private static String PAGE_NAME = "New NesC Project";
	private static String WIZARD_NAME = "NesC Project Wizard";
	private WizardNewProjectCreationPage _pageOne;
	private NescWizardNewProjectGeneralSettingsPage _pageTwo;
	private NescWizardNewProjectAdditionalPaths _pageThree;

	private boolean validProject;

	public NescNewProjectWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addPages() {
		super.addPages();

		_pageOne = new WizardNewProjectCreationPage(PAGE_NAME);
		_pageOne.setTitle("NesC Project");
		_pageOne.setDescription("New NesC project.");
		addPage(_pageOne);

		_pageTwo = new NescWizardNewProjectGeneralSettingsPage(PAGE_NAME);
		_pageTwo.setTitle("Project setup");
		_pageTwo.setDescription("Set the main configuration name and optionally choose the platform.");
		addPage(_pageTwo);

		_pageThree = new NescWizardNewProjectAdditionalPaths(PAGE_NAME);
		_pageThree.setTitle("Additional settings");
		_pageThree.setDescription("Set additional source paths, files included by default or predefined macros.");
		addPage(_pageThree);
	}

	@Override
	public boolean canFinish() {
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		System.out.println("PERFORM FINISH in wizard");
		String name = _pageOne.getProjectName();
		URI location = null;
		if (!_pageOne.useDefaults()) {
			location = _pageOne.getLocationURI();
		} // else location == null

		/* Nature is set in createProject. */
		final IProject project = NescProjectSupport.createProject(name, location);

		try {
			setProjectPreferenceValue(project, MAIN_CONFIGURATION, _pageTwo.getMainConfiguration());
			setProjectPreferenceValue(project, TINYOS_PROJECT, _pageTwo.getTinyOsProject());
			setProjectPreferenceValue(project, TINYOS_PLATFORM, _pageTwo.getTinyOsPlatform());
			setProjectPreferenceValue(project, TINYOS_PREDEFINED_PLATFORM, _pageTwo.isPlatformPredefined());
			setProjectPreferenceValue(project, TINYOS_PATH, _pageTwo.getTinyOsPath());

			setProjectPreferenceValue(project, ADDITIONAL_INCLUDE_PATHS, _pageThree.getAdditionalIncludePaths());
			setProjectPreferenceValue(project, ADDITIONAL_DEFAULT_FILES, _pageThree.getDefaultIncludes());
			setProjectPreferenceValue(project, ADDITIONAL_PREDEFINED_MACROS, _pageThree.getPredefinedMacros());

			setErrorMessage(null);
		} catch (BackingStoreException e) {
			setErrorMessage("Failed to write project configuration on disk");
		}

		/* Build context after saving project settings. */
		try {
			final IRunnableWithProgress job = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Creating project context...", IProgressMonitor.UNKNOWN);
					final Optional<String> msg = ProjectManager.ensureContext(project);
					monitor.done();

					// Rebuild is done automatically by builder.

					if (msg.isPresent()) {
						setErrorMessage(msg.get());
						validProject = false;
					} else {
						validProject = true;
					}
				}
			};
			final ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
			dialog.run(true, true, job);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return validProject;
	}

	private void setErrorMessage(String message) {
		_pageOne.setErrorMessage(message);
		_pageTwo.setErrorMessage(message);
		_pageThree.setErrorMessage(message);
	}
}
