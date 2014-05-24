package pl.edu.mimuw.nesc.plugin.wizards;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import pl.edu.mimuw.nesc.plugin.wizards.composite.PageCompositeListener;
import pl.edu.mimuw.nesc.plugin.wizards.composite.ProjectAdditionalPathSettingsComposite;

/**
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescWizardNewProjectAdditionalPaths extends WizardPage {

	private final PageCompositeListener listener = new PageCompositeListener() {
		@Override
		public void setPageComplete(boolean isComplete) {
			NescWizardNewProjectAdditionalPaths.this.setPageComplete(isComplete);
		}

		@Override
		public void setErrorMessage(String message) {
			NescWizardNewProjectAdditionalPaths.this.setErrorMessage(message);
		}
	};

	private ProjectAdditionalPathSettingsComposite additionalPathsComposite;

	public NescWizardNewProjectAdditionalPaths(String pageName) {
		super(pageName);
		setTitle(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		container.setLayout(layout);
		additionalPathsComposite = new ProjectAdditionalPathSettingsComposite(container, listener);
		setControl(container);
	}

	public List<String> getAdditionalIncludePaths() {
		return additionalPathsComposite.getAdditionalIncludePaths();
	}

	public List<String> getDefaultIncludes() {
		return additionalPathsComposite.getAdditionalDefaultIncludes();
	}

	public List<String> getPredefinedMacros() {
		return additionalPathsComposite.getAdditionalPredefinedMacros();
	}
}
