package pl.edu.mimuw.nesc.plugin.projects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import pl.edu.mimuw.nesc.plugin.wizards.composite.PageCompositeListener;

/**
 * Property page base class for NesC projects.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public abstract class NescPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	protected PageCompositeListener listener = new PageCompositeListener() {
		@Override
		public void setPageComplete(boolean isComplete) {
			NescPropertyPage.this.isComplete = isComplete;
		}

		@Override
		public void setErrorMessage(String message) {
			NescPropertyPage.this.setErrorMessage(message);
		}
	};

	protected boolean isComplete = true;

	@Override
	public void performDefaults() {
		initializeDefaults();
	}

	@Override
	public boolean performOk() {
		/*
		 * NOTE: In wizard this method is never called. See wizard's onFinish()
		 * method.
		 */
		if (isComplete) {
			storeValues();
		}
		return isComplete;
	}

	/**
	 * Gets current project.
	 *
	 * @return current project.
	 */
	protected IProject getProject() {
		IProject project = null;
		IAdaptable elem = getElement();
		if (elem instanceof IProject) {
			project = (IProject) elem;
		} else if (elem != null) {
			project = (IProject) elem.getAdapter(IProject.class);
		}
		return project;
	}

	/**
	 * Restores default properties (e.g. when user presses <b>Restore
	 * Defaults</b>.
	 */
	protected abstract void initializeDefaults();

	/**
	 * Initializes all controls with current preference values.
	 */
	protected abstract void initializeValues();

	/**
	 * Saves the current values of the controls on the property page into
	 * property store.
	 */
	protected abstract void storeValues();

}
