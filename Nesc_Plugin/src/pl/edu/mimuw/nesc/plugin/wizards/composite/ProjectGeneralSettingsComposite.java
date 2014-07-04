package pl.edu.mimuw.nesc.plugin.wizards.composite;

import java.util.ArrayList;
import java.util.List;

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

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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
	private final List<PlatformItem> platforms;

	private Text mainConfiguration;
	private Button tinyOsProject;
	private Combo tinyOsPlatform;
	private DirectorySelector tinyOsPathSelector;

	private Listener pageModifyListener = new Listener() {
		@Override
		public void handleEvent(Event e) {
			pageCompositeListener.setPageComplete(validatePage());
		}
	};

	public ProjectGeneralSettingsComposite(Composite parent, PageCompositeListener pageCompositeListener) {
		super(parent, SWT.NONE);
		this.pageCompositeListener = pageCompositeListener;
		this.platforms = getPlatforms();
		createControl(parent);
	}

	private void createControl(Composite parent) {
		/* Prepare container. */
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout layout = new GridLayout(2, false);
		setLayout(layout);
		setLayoutData(parentData);

		/* Main configuration selector. */
		Label configurationLabel = new Label(this, SWT.NONE);
		configurationLabel.setText(MAIN_CONFIGURATION_LABEL);
		mainConfiguration = new Text(this, SWT.BORDER | SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		mainConfiguration.setLayoutData(data);
		mainConfiguration.setText(EMTPY_STRING);
		mainConfiguration.addListener(SWT.Modify, pageModifyListener);

		/* TinyOS project checkbox. */
		Label labelTinyOS = new Label(this, SWT.NONE);
		labelTinyOS.setText(TINYOS_PROJECT_LABEL);
		tinyOsProject = new Button(this, SWT.CHECK);
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
		Label labelPlatform = new Label(this, SWT.NONE);
		labelPlatform.setText(PLATFORM_LABEL);
		tinyOsPlatform = new Combo(this, SWT.READ_ONLY);
		tinyOsPlatform.setItems(getPlatformNames(platforms));
		tinyOsPlatform.setEnabled(TINYOS_PROJECT_SELECTION_DEFAULT);
		tinyOsPlatform.addListener(SWT.Modify, pageModifyListener);

		/* TinyOS directory selector. */
		Label tinyOsPath = new Label(this, SWT.NONE);
		tinyOsPath.setText(TINYOS_PATH_LABEL);
		tinyOsPathSelector = new DirectorySelector(this);
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
		final int index = tinyOsPlatform.getSelectionIndex();
		if (index < 0) {
			return EMTPY_STRING;
		}
		return platforms.get(index).getName();
	}

	public boolean isPlatformPredefined() {
		if (tinyOsPlatform == null) {
			return false;
		}
		final int index = tinyOsPlatform.getSelectionIndex();
		if (index < 0) {
			return false;
		}
		return platforms.get(index).isPredefined();
	}

	public String getTinyOsPath() {
		if (tinyOsPathSelector == null) {
			return EMTPY_STRING;
		}
		return emptyStringIfNull(tinyOsPathSelector.getSelectedPath());
	}

	public void setData(String mainConfiguration, boolean tinyOsProject, String platform, boolean predefinedPlatform,
			String platformPath) {
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

			for (int i = 0; i < platforms.size(); ++i) {
				final PlatformItem item = platforms.get(i);
				if (item.isPredefined() == predefinedPlatform && item.getName().equals(platform)) {
					this.tinyOsPlatform.select(i);
					platformExists = true;
					break;
				}
			}
			if (!platformExists) {
				pageCompositeListener.setErrorMessage("Unknown platform '" + platform + "'. Select a new one.");
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

	private List<PlatformItem> getPlatforms() {
		final List<PlatformItem> result = new ArrayList<>();
		for (String name : NescPlatformUtil.getPredefinedPlatforms()) {
			result.add(new PlatformItem(name, true));
		}
		for (String name : NescPlatformUtil.getUserDefinedPlatforms()) {
			result.add(new PlatformItem(name, false));
		}
		return result;
	}

	private String[] getPlatformNames(List<PlatformItem> platforms) {
		return Lists.transform(platforms, new Function<PlatformItem, String>() {
			@Override
			public String apply(PlatformItem item) {
				return item.getDisplayName();
			}
		}).toArray(new String[platforms.size()]);
	}

	/**
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 */
	private static final class PlatformItem {

		private final String name;
		private final boolean predefined;
		private final String displayName;

		public PlatformItem(String name, boolean predefined) {
			this.name = name;
			this.predefined = predefined;
			this.displayName = name + (predefined ? " (predefined)" : "");
		}

		public String getName() {
			return name;
		}

		public boolean isPredefined() {
			return predefined;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
}
