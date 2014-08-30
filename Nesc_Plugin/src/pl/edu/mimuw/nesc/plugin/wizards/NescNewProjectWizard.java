package pl.edu.mimuw.nesc.plugin.wizards;

import static pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences.*;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.frontend.FrontendManager;
import pl.edu.mimuw.nesc.plugin.projects.pages.NescProjectSupport;
import pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences;
import pl.edu.mimuw.nesc.plugin.variable.PathVariable;

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
	private NescWizardNewProjectAdditionalMacrosPage _pageThree;

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

		_pageThree = new NescWizardNewProjectAdditionalMacrosPage(PAGE_NAME);
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
		}

		/* Nature is set in createProject. */
		final IProject project = NescProjectSupport.createProject(name, location);

		try {
			NescProjectPreferences.transaction(project)
					.set(MAIN_CONFIGURATION, _pageTwo.getMainConfiguration())
					.set(TINYOS_PLATFORM, _pageTwo.getTinyOsPlatform())
					.set(TINYOS_PREDEFINED_PLATFORM, _pageTwo.isPlatformPredefined())
					.set(TINYOS_PATH, _pageTwo.getTinyOsPath())
					.set(NCLIB_PATH, _pageTwo.getNescLibPath())
					.set(CLIB_PATH, _pageTwo.getClibPath())
					.set(HWLIB_PATH, _pageTwo.getHwlibPath())
					.set(ADDITIONAL_DEFAULT_FILES, _pageThree.getDefaultIncludes())
					.set(ADDITIONAL_PREDEFINED_MACROS, _pageThree.getPredefinedMacros())
					.commit();

			final IPathVariableManager pathManager = project.getPathVariableManager();
			pathManager.setURIValue(PathVariable.OSDIR_NAME, new URI(_pageTwo.getTinyOsPath()));
			pathManager.setURIValue(PathVariable.NCLIBDIR_NAME, new URI(_pageTwo.getNescLibPath()));
			pathManager.setURIValue(PathVariable.CLIBDIR_NAME, new URI(_pageTwo.getClibPath()));
			pathManager.setURIValue(PathVariable.HWLIBDIR_NAME, new URI(_pageTwo.getHwlibPath()));

			setErrorMessage(null);
		} catch (BackingStoreException e) {
			setErrorMessage("Failed to write project configuration on disk");
		} catch (CoreException | URISyntaxException  e) {
			setErrorMessage("Failed to save project configuration");
			e.printStackTrace();
		}

		/* Build context after saving project settings. */
		try {
			final IRunnableWithProgress job = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Creating project context...", IProgressMonitor.UNKNOWN);
					final Optional<String> msg = FrontendManager.ensureContext(project);
					monitor.done();

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
