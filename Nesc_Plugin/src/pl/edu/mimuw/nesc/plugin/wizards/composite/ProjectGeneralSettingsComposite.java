package pl.edu.mimuw.nesc.plugin.wizards.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import pl.edu.mimuw.nesc.plugin.projects.util.NescPlatformUtil;

/**
 * A composite for setting project general properties. It is a main part of both
 * a new project wizard page and project property page.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class ProjectGeneralSettingsComposite extends Composite {

	private static final String EMTPY_STRING = ""; //$NON-NLS-1$
	private static final String MAIN_CONFIGURATION_LABEL = "Main configuration:";//$NON-NLS-1$
	private static final String TINYOS_PROJECT_LABEL = "TinyOS project:";//$NON-NLS-1$
	private static final String PLATFORM_LABEL = "Platform:";//$NON-NLS-1$
	private static final String TINYOS_PATH_LABEL = "TinyOS path:";//$NON-NLS-1$

	private static final String ERROR_MAIN_CONFIG_NOT_SET = "Main configuration is not set.";//$NON-NLS-1$
	private static final String ERROR_TINYOS_PLATFORM_NOT_SET = "TinyOS platform is not set.";//$NON-NLS-1$
	private static final String ERROR_TINYOS_PATH_NOT_SET = "TinyOS path is not set.";//$NON-NLS-1$

	private static final boolean TINYOS_PROJECT_SELECTION_DEFAULT = true;

	private final PageCompositeListener pageCompositeListener;

	private Composite container;
	private Text mainConfiguration;
	private Button tinyOsProject;
	private Combo tinyOsPlatform;
	private DirectorySelector tinyOsPathSelector;

	private Listener pageModifyListener = new Listener() {
		@Override
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			pageCompositeListener.setPageComplete(valid);
		}
	};

	public ProjectGeneralSettingsComposite(Composite parent, PageCompositeListener pageCompositeListener) {
		super(parent, SWT.NONE);
		this.pageCompositeListener = pageCompositeListener;
		createControl(parent);
	}

	private void createControl(Composite parent) {
		/* Prepare container. */
		container = new Composite(parent, SWT.NONE);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);
		container.setLayoutData(parentData);

		/* Main configuration selector. */
		Label configurationLabel = new Label(container, SWT.NONE);
		configurationLabel.setText(MAIN_CONFIGURATION_LABEL);
		mainConfiguration = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		mainConfiguration.setLayoutData(data);
		mainConfiguration.setText(EMTPY_STRING);
		mainConfiguration.addListener(SWT.Modify, pageModifyListener);

		/* TinyOS project checkbox. */
		Label labelTinyOS = new Label(container, SWT.NONE);
		labelTinyOS.setText(TINYOS_PROJECT_LABEL);
		tinyOsProject = new Button(container, SWT.CHECK);
		tinyOsProject.setSelection(TINYOS_PROJECT_SELECTION_DEFAULT);
		tinyOsProject.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean isSelected = ((Button) e.widget).getSelection();
				if (tinyOsPlatform != null) {
					tinyOsPlatform.setEnabled(isSelected);
				}
				if (tinyOsPathSelector != null) {
					tinyOsPathSelector.setEnabled(isSelected);
				}
				// FIXME: hack: modify listener for check button does not work?
				pageModifyListener.handleEvent(null);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		// FIXME: modify listener for check button does not work?
		// tinyOsProject.addListener(SWT.Modify, pageModifyListener);

		/* Platform selector. */
		Label labelPlatform = new Label(container, SWT.NONE);
		labelPlatform.setText(PLATFORM_LABEL);
		tinyOsPlatform = new Combo(container, SWT.READ_ONLY);
		tinyOsPlatform.setItems(getAvailablePlatforms());
		tinyOsPlatform.setEnabled(TINYOS_PROJECT_SELECTION_DEFAULT);
		tinyOsPlatform.addListener(SWT.Modify, pageModifyListener);

		/* TinyOS directory selector. */
		Label tinyOsPath = new Label(container, SWT.NONE);
		tinyOsPath.setText(TINYOS_PATH_LABEL);
		tinyOsPathSelector = new DirectorySelector(container);
		tinyOsPathSelector.setEnabled(TINYOS_PROJECT_SELECTION_DEFAULT);
		tinyOsPathSelector.addListener(SWT.Modify, pageModifyListener);

		/* Required to avoid an error in the system. */
		pageCompositeListener.setPageComplete(validatePage());
	}

	public String getMainConfiguration() {
		if (mainConfiguration == null) {
			return EMTPY_STRING;
		}
		return emptyStringIfNull(mainConfiguration.getText());
	}

	public boolean isTinyOsProject() {
		if (tinyOsProject == null) {
			return false;
		}
		return tinyOsProject.getSelection();
	}

	public String getTinyOsPlatform() {
		if (tinyOsPlatform == null) {
			return EMTPY_STRING;
		}
		return emptyStringIfNull(tinyOsPlatform.getText());
	}

	public String getTinyOsPath() {
		if (tinyOsPathSelector == null) {
			return EMTPY_STRING;
		}
		return emptyStringIfNull(tinyOsPathSelector.getSelectedPath());
	}

	public void setData(String mainConfiguration, boolean tinyOsProject, String platform, String platformPath) {
		if (this.mainConfiguration != null) {
			this.mainConfiguration.setText(mainConfiguration);
		}
		if (this.tinyOsProject != null) {
			this.tinyOsProject.setSelection(tinyOsProject);
		}
		if (!tinyOsProject) {
			return;
		}
		if (this.tinyOsPlatform != null) {
			boolean platformExists = false;
			final String[] items = this.tinyOsPlatform.getItems();
			for (int i = 0; i < items.length; ++i) {
				final String item = items[i];
				if (items != null && item.equals(platform)) {
					this.tinyOsPlatform.select(i);
					platformExists = true;
					break;
				}
			}
			if (!platformExists) {
				pageCompositeListener.setErrorMessage("Invalid platform '" + platform + "'");
				pageCompositeListener.setPageComplete(false);
			}
		}
		if (this.tinyOsPathSelector != null) {
			this.tinyOsPathSelector.setPath(platformPath);
		}
	}

	private boolean validatePage() {
		if (getMainConfiguration().isEmpty()) {
			pageCompositeListener.setErrorMessage(ERROR_MAIN_CONFIG_NOT_SET);
			return false;
		}
		if (isTinyOsProject()) {
			if (getTinyOsPlatform().isEmpty()) {
				pageCompositeListener.setErrorMessage(ERROR_TINYOS_PLATFORM_NOT_SET);
				return false;
			}
			if (getTinyOsPath().isEmpty()) {
				pageCompositeListener.setErrorMessage(ERROR_TINYOS_PATH_NOT_SET);
				return false;
			}
		}
		/* Page is valid. */
		pageCompositeListener.setErrorMessage(null);
		return true;
	}

	private String emptyStringIfNull(String str) {
		return str == null ? EMTPY_STRING : str;
	}

	private String[] getAvailablePlatforms() {
		return NescPlatformUtil.getAvailablePlatforms();
	}
}
