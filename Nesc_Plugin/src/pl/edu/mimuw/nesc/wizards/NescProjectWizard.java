package pl.edu.mimuw.nesc.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import pl.edu.mimuw.nesc.projects.NescProjectSupport;

public class NescProjectWizard extends Wizard implements INewWizard {

	private static String PAGE_NAME = "New nesC Project";
	private static String WIZARD_NAME = "nesC Project Wizard";
	private WizardNewProjectCreationPage _pageOne;
	private NescWizardNewProjectMainConfiguartionPage _pageTwo;

	public NescProjectWizard() {
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
		_pageOne.setTitle("nesC Project");
		_pageOne.setDescription("New nesC project");
		
		addPage(_pageOne);
		
		_pageTwo = new NescWizardNewProjectMainConfiguartionPage(PAGE_NAME);
		_pageTwo.setTitle("Project setup");
		_pageTwo.setDescription("Set the main configuration name and optionally choose the platform");
		
		addPage(_pageTwo);
	}
	
	@Override
	public boolean canFinish() {
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		String name = _pageOne.getProjectName();
		URI location = null;
		if (!_pageOne.useDefaults()) {
			location = _pageOne.getLocationURI();
		} // else location == null

		IProject project = NescProjectSupport.createProject(name, location);
		
		try {
			project.setPersistentProperty(
					NescProjectSupport.getProjectMainConfQualName(project.getName()),
					_pageTwo.getMainConfiguration());
			project.setPersistentProperty(
					NescProjectSupport.getProjectIsTinyOsProjQualName(project.getName()),
					Boolean.toString(_pageTwo.getTinyOsProject()));
			project.setPersistentProperty(
					NescProjectSupport.getProjectTinyOsPlatformQualName(project.getName()),
					_pageTwo.getTinyOsPlatform());
			_pageOne.setErrorMessage(null);
			_pageTwo.setErrorMessage(null);
			
		} catch (CoreException e) {
			String error = "Failed to set project information!";
			_pageOne.setErrorMessage(error);
			_pageTwo.setErrorMessage(error);
			return false;
		}

		return true;
	}

}
