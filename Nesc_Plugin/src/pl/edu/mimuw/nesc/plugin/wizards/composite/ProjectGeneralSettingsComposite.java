package pl.edu.mimuw.nesc.plugin.wizards.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import pl.edu.mimuw.nesc.plugin.preferences.NescPluginPreferences;
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

	private static final String EMTPY_STRING = "";
	private static final String MAIN_CONFIGURATION_LABEL = "Main configuration:";
	private static final String PLATFORM_LABEL = "Platform:";
	private static final String TINYOS_LOC_LABEL = "TinyOS path:";
	private static final String NESCLIB_LOC_LABEL = "nclib path:";
	private static final String CLIB_LOC_LABEL = "clib path:";
	private static final String HWLIB_LOC_LABEL = "hwlib path:";

	private static final String ERROR_MAIN_CONFIG_NOT_SET = "Main configuration is not set.";
	private static final String ERROR_TINYOS_PLATFORM_NOT_SET = "TinyOS platform is not set.";
	private static final String ERROR_TINYOS_PATH_NOT_SET = "TinyOS path is not set.";
	private static final String ERROR_NESCLIB_PATH_NOT_SET = "NesC lib path is not set.";
	private static final String ERROR_CLIB_PATH_NOT_SET = "C lib path is not set.";
	private static final String ERROR_HWLIB_PATH_NOT_SET = "Hardware lib path is not set.";

	private final PageCompositeListener pageCompositeListener;
	private final List<PlatformItem> platforms;

	private Text mainConfiguration;
	private Combo tinyOsPlatform;
	private DirectorySelector tinyOsLocSelector;
	private DirectorySelector nescLibLocSelector;
	private DirectorySelector clibLocSelector;
	private DirectorySelector hwlibLocSelector;

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

		/* Platform selector. */
		Label labelPlatform = new Label(this, SWT.NONE);
		labelPlatform.setText(PLATFORM_LABEL);
		tinyOsPlatform = new Combo(this, SWT.READ_ONLY);
		tinyOsPlatform.setItems(getPlatformNames(platforms));
		tinyOsPlatform.addListener(SWT.Modify, pageModifyListener);

		/* TinyOS directory selector. */
		Label tinyOsPath = new Label(this, SWT.NONE);
		tinyOsPath.setText(TINYOS_LOC_LABEL);
		tinyOsLocSelector = new DirectorySelector(this);
		tinyOsLocSelector.addListener(SWT.Modify, pageModifyListener);
		tinyOsLocSelector.setPath(NescPluginPreferences.getString(NescPluginPreferences.OS_LOC));

		/* NesC directory selector. */
		Label nescLibPath = new Label(this, SWT.NONE);
		nescLibPath.setText(NESCLIB_LOC_LABEL);
		nescLibLocSelector = new DirectorySelector(this);
		nescLibLocSelector.addListener(SWT.Modify, pageModifyListener);
		nescLibLocSelector.setPath(NescPluginPreferences.getString(NescPluginPreferences.NCLIB_LOC));

		/* C directory selector. */
		Label clibPath = new Label(this, SWT.NONE);
		clibPath.setText(CLIB_LOC_LABEL);
		clibLocSelector = new DirectorySelector(this);
		clibLocSelector.addListener(SWT.Modify, pageModifyListener);
		clibLocSelector.setPath(NescPluginPreferences.getString(NescPluginPreferences.CLIB_LOC));

		/* Hardware directory selector. */
		Label hwlibPath = new Label(this, SWT.NONE);
		hwlibPath.setText(HWLIB_LOC_LABEL);
		hwlibLocSelector = new DirectorySelector(this);
		hwlibLocSelector.addListener(SWT.Modify, pageModifyListener);
		hwlibLocSelector.setPath(NescPluginPreferences.getString(NescPluginPreferences.HWLIB_LOC));

		/* Required to avoid an error in the system. */
		pageCompositeListener.setPageComplete(validatePage());
	}

	public String getMainConfiguration() {
		if (mainConfiguration == null) {
			return EMTPY_STRING;
		}
		return emptyStringIfNull(mainConfiguration.getText());
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
		if (tinyOsLocSelector == null) {
			return EMTPY_STRING;
		}
		return emptyStringIfNull(tinyOsLocSelector.getSelectedPath());
	}

	public String getNescLibPath() {
		if (nescLibLocSelector == null) {
			return EMTPY_STRING;
		}
		return emptyStringIfNull(nescLibLocSelector.getSelectedPath());
	}

	public String getClibPath() {
		if (clibLocSelector == null) {
			return EMTPY_STRING;
		}
		return emptyStringIfNull(clibLocSelector.getSelectedPath());
	}

	public String getHwlibPath() {
		if (hwlibLocSelector == null) {
			return EMTPY_STRING;
		}
		return emptyStringIfNull(hwlibLocSelector.getSelectedPath());
	}

	public void setData(String mainConfiguration, String platform, boolean predefinedPlatform, String platformPath,
			String nescLibPath, String clibPath, String hwlibPath) {
		if (this.mainConfiguration != null) {
			this.mainConfiguration.setText(mainConfiguration);
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
		if (this.tinyOsLocSelector != null) {
			this.tinyOsLocSelector.setPath(platformPath);
		}
		if (this.nescLibLocSelector != null) {
			this.nescLibLocSelector.setPath(nescLibPath);
		}
		if (this.clibLocSelector != null) {
			this.clibLocSelector.setPath(clibPath);
		}
		if (this.hwlibLocSelector != null) {
			this.hwlibLocSelector.setPath(hwlibPath);
		}
	}

	private boolean validatePage() {
		if (getMainConfiguration().isEmpty()) {
			pageCompositeListener.setErrorMessage(ERROR_MAIN_CONFIG_NOT_SET);
			return false;
		}
		if (getTinyOsPlatform().isEmpty()) {
			pageCompositeListener.setErrorMessage(ERROR_TINYOS_PLATFORM_NOT_SET);
			return false;
		}
		if (getTinyOsPath().isEmpty()) {
			pageCompositeListener.setErrorMessage(ERROR_TINYOS_PATH_NOT_SET);
			return false;
		}
		if (getNescLibPath().isEmpty()) {
			pageCompositeListener.setErrorMessage(ERROR_NESCLIB_PATH_NOT_SET);
			return false;
		}
		if (getClibPath().isEmpty()) {
			pageCompositeListener.setErrorMessage(ERROR_CLIB_PATH_NOT_SET);
			return false;
		}
		if (getHwlibPath().isEmpty()) {
			pageCompositeListener.setErrorMessage(ERROR_HWLIB_PATH_NOT_SET);
			return false;
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
