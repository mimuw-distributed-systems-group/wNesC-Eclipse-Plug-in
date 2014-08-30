package pl.edu.mimuw.nesc.plugin.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import pl.edu.mimuw.nesc.plugin.wizards.composite.PageCompositeListener;
import pl.edu.mimuw.nesc.plugin.wizards.composite.ProjectGeneralSettingsComposite;

/**
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *         <p>
 *         TinyOS platform and TinyOS path selectors. Refactoring.
 *         </p>
 *
 */
public class NescWizardNewProjectGeneralSettingsPage extends WizardPage {

	private final PageCompositeListener listener = new PageCompositeListener() {
		@Override
		public void setPageComplete(boolean isComplete) {
			NescWizardNewProjectGeneralSettingsPage.this.setPageComplete(isComplete);
		}

		@Override
		public void setErrorMessage(String message) {
			NescWizardNewProjectGeneralSettingsPage.this.setErrorMessage(message);
		}
	};

	private ProjectGeneralSettingsComposite generalSettingsComposite;

	public NescWizardNewProjectGeneralSettingsPage(String pageName) {
		super(pageName);
		setTitle(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		container.setLayout(layout);

		generalSettingsComposite = new ProjectGeneralSettingsComposite(container, listener);
		setControl(container);
	}

	public String getMainConfiguration() {
		return generalSettingsComposite.getMainConfiguration();
	}

	public String getTinyOsPlatform() {
		return generalSettingsComposite.getTinyOsPlatform();
	}

	public boolean isPlatformPredefined() {
		return generalSettingsComposite.isPlatformPredefined();
	}

	public String getTinyOsPath() {
		return generalSettingsComposite.getTinyOsPath();
	}

	public String getNescLibPath() {
		return generalSettingsComposite.getNescLibPath();
	}

	public String getClibPath() {
		return generalSettingsComposite.getClibPath();
	}

	public String getHwlibPath() {
		return generalSettingsComposite.getHwlibPath();
	}
}
