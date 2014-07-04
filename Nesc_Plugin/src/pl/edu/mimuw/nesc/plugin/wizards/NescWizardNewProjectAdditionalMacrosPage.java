package pl.edu.mimuw.nesc.plugin.wizards;

import static org.eclipse.swt.SWT.FILL;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import pl.edu.mimuw.nesc.plugin.wizards.composite.PageCompositeListener;
import pl.edu.mimuw.nesc.plugin.wizards.composite.ProjectAdditionalMacrosComposite;

/**
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescWizardNewProjectAdditionalMacrosPage extends WizardPage {

	private final PageCompositeListener listener = new PageCompositeListener() {
		@Override
		public void setPageComplete(boolean isComplete) {
			NescWizardNewProjectAdditionalMacrosPage.this.setPageComplete(isComplete);
		}

		@Override
		public void setErrorMessage(String message) {
			NescWizardNewProjectAdditionalMacrosPage.this.setErrorMessage(message);
		}
	};

	private ProjectAdditionalMacrosComposite additionalPathsComposite;

	public NescWizardNewProjectAdditionalMacrosPage(String pageName) {
		super(pageName);
		setTitle(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		GridData layoutData = new GridData(FILL, FILL, true, true);
		container.setLayout(layout);
		container.setLayoutData(layoutData);
		additionalPathsComposite = new ProjectAdditionalMacrosComposite(container, listener);
		setControl(container);
	}

	public List<String> getDefaultIncludes() {
		return additionalPathsComposite.getAdditionalDefaultIncludes();
	}

	public List<String> getPredefinedMacros() {
		return additionalPathsComposite.getAdditionalPredefinedMacros();
	}
}
