package pl.edu.mimuw.nesc.plugin.wizards;

import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_DEFAULT_FILES;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.ADDITIONAL_PREDEFINED_MACROS;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.MAIN_CONFIGURATION;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PATH;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PLATFORM;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PREDEFINED_PLATFORM;
import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.TINYOS_PROJECT;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.frontend.FrontendManager;
import pl.edu.mimuw.nesc.plugin.projects.pages.NescProjectSupport;
import pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences;

import com.google.common.base.Optional;

public class NescImportProjectWizard extends Wizard implements IImportWizard {
	/**
	 * Identifier of the wizard from 'plugin.xml' file.
	 */
	public static final String WIZARD_ID = "pl.edu.mimuw.nesc.wizards.import.NescImportWizard";

	private static String PAGE_NAME = "Import NesC Project";
	private static String WIZARD_NAME = "NesC Import Project Wizard";
	private WizardImportProjectCreationPage _pageOne; // <- redo
	private NescWizardNewProjectGeneralSettingsPage _pageTwo;
	private NescWizardNewProjectAdditionalMacrosPage _pageThree;

	private boolean validProject;

	public NescImportProjectWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addPages() {
		super.addPages();

		_pageOne = new WizardImportProjectCreationPage(PAGE_NAME);
		_pageOne.setTitle("NesC Project");
		_pageOne.setDescription("New NesC project.");
		addPage(_pageOne);

		_pageTwo = new NescWizardNewProjectGeneralSettingsPage(PAGE_NAME);
		_pageTwo.setTitle("Project setup");
		_pageTwo.setDescription("Set the main configuration name and optionally choose the platform.");
		addPage(_pageTwo);

		_pageThree = new NescWizardNewProjectAdditionalMacrosPage(PAGE_NAME);
		_pageThree.setTitle("Additional settings");
		_pageThree.setDescription("Set files included by default and predefined macros.");
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
		URI location = _pageOne.getLocationURI();

		/* Nature is set in createProject. */
		final IProject project = NescProjectSupport.createProject(name, location);

		try {
			NescProjectPreferences.transaction(project)
					.set(MAIN_CONFIGURATION, _pageTwo.getMainConfiguration())
					.set(TINYOS_PROJECT, _pageTwo.getTinyOsProject())
					.set(TINYOS_PLATFORM, _pageTwo.getTinyOsPlatform())
					.set(TINYOS_PREDEFINED_PLATFORM, _pageTwo.isPlatformPredefined())
					.set(TINYOS_PATH, _pageTwo.getTinyOsPath())
					.set(ADDITIONAL_DEFAULT_FILES, _pageThree.getDefaultIncludes())
					.set(ADDITIONAL_PREDEFINED_MACROS, _pageThree.getPredefinedMacros())
					.commit();

			setErrorMessage(null);
		} catch (BackingStoreException e) {
			setErrorMessage("Failed to write project configuration on disk");
		}

		try {
			NescProjectSupport.importAllFilesInProjectFolder(project);
		} catch (CoreException e) {
			setErrorMessage("Failed to import files from project folder");
		}

		/* Build context after saving project settings. */
		try {
			final IRunnableWithProgress job = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Creating project context...", IProgressMonitor.UNKNOWN);
					final Optional<String> msg = FrontendManager.ensureContext(project);
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
