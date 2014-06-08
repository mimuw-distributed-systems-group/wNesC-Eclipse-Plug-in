package pl.edu.mimuw.nesc.plugin.wizards;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class WizardImportProjectCreationPage extends WizardPage {

	private Composite container;

	private Text projectName;
	private Text location;
	private Button directorySearchButton;

	private Listener pageModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(validatePage());
		}
	};

	protected WizardImportProjectCreationPage(String pageName) {
		super(pageName);
		setTitle(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;

		Label projectNameLabel = new Label(container, SWT.NONE);
		projectNameLabel.setText("Project name:");
		projectName = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		projectName.setLayoutData(data);
		projectName.setText("");
		projectName.addListener(SWT.Modify, pageModifyListener);

		Label directorySearchLabel = new Label(container, SWT.NONE);
		directorySearchLabel.setText("Project directory:");
		location = new Text(container, SWT.BORDER | SWT.SINGLE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		location.setLayoutData(data);
		location.setText("");
		directorySearchButton = new Button(container, SWT.PUSH);
		directorySearchButton.setText("Browse");
		directorySearchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				String directory = dialog.open();
				if (directory != null) {
					location.setText(directory);
				}
				if (Paths.get(directory).toFile().isDirectory()) {
					projectName.setText(Paths.get(directory).getFileName()
							.toString());
				}
				pageModifyListener.handleEvent(null);
			}
		});

		// Required to avoid an error in the system
		setControl(container);
		setPageComplete(validatePage());
	}

	protected boolean validatePage() {
		this.setErrorMessage(null);
		String name = projectName.getText();
		if (!name.isEmpty()) {
			IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(name);
			if (newProject.exists()) {
				this.setErrorMessage("project with such a name already exists");
				return false;
			}
		}
		File projectDir = Paths.get(location.getText()).toFile();
		if (!projectDir.exists()) {
			this.setErrorMessage("No such file or directory");
			return false;
		}
		if (!projectDir.isDirectory()) {
			this.setErrorMessage("Path does not represent a directory");
			return false;
		}
		return true;
	}

	public String getProjectName() {
		return projectName.getText();
	}

	public URI getLocationURI() {
		return Paths.get(location.getText()).toUri();
	}

}
