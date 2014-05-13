package pl.edu.mimuw.nesc.plugin.projects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.projects.util.NescProjectPreferences;
import pl.edu.mimuw.nesc.plugin.projects.util.ProjectUtil;

public class NescProjectPage extends PropertyPage implements IWorkbenchPropertyPage {
	private boolean isProjectLevel;
	private Text mainConfiguration;
	private Button isTinyOsProject;
	private Text tinyOsPlatform;

	@Override
	protected Control createContents(Composite parent) {
		IProject project = getProject();
		isProjectLevel = (project != null);
		if(isProjectLevel) {
			Composite contents = new Composite(parent, SWT.NULL);
			contents.setFont(parent.getFont());

			contents.setLayout(new GridLayout(3, true));
			contents.setLayoutData(new GridData(GridData.FILL_BOTH));
			((GridLayout) contents.getLayout()).makeColumnsEqualWidth = false;

			Label label= new Label(parent, SWT.LEFT);
			GridData gd= new GridData();
			gd.horizontalAlignment= GridData.BEGINNING;
			gd.grabExcessHorizontalSpace= false;
			gd.horizontalSpan= 3;
			gd.horizontalIndent= 0;
			gd.widthHint= 0;
			gd.heightHint= 0;
			label.setLayoutData(gd);
			
			label = new Label(contents, SWT.NULL);
			label.setText("Main configuration:");
			mainConfiguration = new Text(contents, SWT.BORDER | SWT.SINGLE);
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.verticalAlignment = GridData.CENTER;
			gd.grabExcessVerticalSpace = false;
			gd.horizontalSpan = 2;
			mainConfiguration.setLayoutData(gd);
			
			label = new Label(contents, SWT.NULL);
			label.setText("TinyOS project:");
			isTinyOsProject = new Button(contents, SWT.CHECK);
			gd = new GridData();
			gd.horizontalSpan = 2;
			isTinyOsProject.setLayoutData(gd);
			isTinyOsProject.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (((Button)e.widget).getSelection()) {
						if (tinyOsPlatform != null) {
							tinyOsPlatform.setEnabled(true);
						}
					} else {
						if (tinyOsPlatform != null) {
							tinyOsPlatform.setEnabled(false);
						}
					}
					
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
		    	
		    });
			
			label = new Label(contents, SWT.NULL);
			label.setText("TinyOS platform:");
			tinyOsPlatform = new Text(contents, SWT.BORDER | SWT.SINGLE);
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.verticalAlignment = GridData.CENTER;
			gd.grabExcessVerticalSpace = false;
			gd.horizontalSpan = 2;
			tinyOsPlatform.setLayoutData(gd);
			
			setProjectData();
		}
		return parent;
	}
	
	private IProject getProject() {
		IProject project= null;
		IAdaptable elem = getElement();
		if (elem instanceof IProject) {
			project= (IProject) elem;
		} else if (elem != null) {
			project= (IProject) elem.getAdapter(IProject.class);
		}
		return project;
	}
	
	@Override
	public void performDefaults() {
		setProjectData();
	}
	
	@Override
	public boolean performOk() {
		saveChanges();
		return true;
	}
	
	private void saveChanges() {
		IProject project = getProject();
		if (project != null) {
			try {
				ProjectUtil.setProjectPreferenceValue(project, NescProjectPreferences.MAIN_CONFIGURATION, mainConfiguration.getText());
				ProjectUtil.setProjectPreferenceValue(project, NescProjectPreferences.TINY_OS_PROJECT, isTinyOsProject.getSelection());
				ProjectUtil.setProjectPreferenceValue(project, NescProjectPreferences.TINY_OS_PLATFORM, tinyOsPlatform.getText());
				this.setErrorMessage(null);
			} catch (BackingStoreException e) {
				this.setErrorMessage("Failed to save changes to project properties");
			}
			
		} else {
			System.err.println("No project found!");
		}
	}
	
	private void setProjectData() {
		IProject project = getProject();
		if (project != null) {
			mainConfiguration.setText(ProjectUtil.getProjectPreferenceValue(project, NescProjectPreferences.MAIN_CONFIGURATION));
			isTinyOsProject.setSelection(ProjectUtil.getProjectPreferenceValueB(project, NescProjectPreferences.TINY_OS_PROJECT));
			tinyOsPlatform.setText(ProjectUtil.getProjectPreferenceValue(project, NescProjectPreferences.TINY_OS_PLATFORM));
			tinyOsPlatform.setEnabled(isTinyOsProject.getSelection());
		}
	}

}
