package pl.edu.mimuw.nesc.plugin.wizards.composite;

import static org.eclipse.swt.SWT.FILL;
import static org.eclipse.swt.SWT.NONE;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import pl.edu.mimuw.nesc.plugin.wizards.fields.AbstractField;
import pl.edu.mimuw.nesc.plugin.wizards.fields.SingleStringField;
import pl.edu.mimuw.nesc.plugin.wizards.fields.SingleStringField.StringValue;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * A composite for selecting additional paths, files or macros. It is a main
 * part of both a new project wizard page and project property page.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class ProjectAdditionalPathSettingsComposite extends Composite {

	private static final int COLUMN_WIDTH = 400;
	private static final String TOSDIR_VARIABLE = "Use ${TOSDIR} variable to refer to TinyOS directory.";//$NON-NLS-1$

	private static final String INCLUDE_PATHS_FIELD = "Include paths";//$NON-NLS-1$
	private static final String INCLUDE_PATHS_TIP = "Directory to be searched for source and header files.";//$NON-NLS-1$
	private static final String ADD_INCLUDE_PATH_TITLE = "New include path";//$NON-NLS-1$
	private static final String ADD_INCLUDE_PATH_MESSAGE = "Enter a path of directory to be searched for source and header files.";//$NON-NLS-1$
	private static final String EDIT_INCLUDE_PATH_TITLE = "Edit include path";//$NON-NLS-1$
	private static final String EDIT_INCLUDE_PATH_MESSAGE = "Edit the path of directory to be searched for source and header files.";//$NON-NLS-1$

	private static final String DEFAULT_INCLUDES_FIELD = "Default includes";//$NON-NLS-1$
	private static final String DEFAULT_INCLUDES_TIP = "File that should be included to each source and header file.";//$NON-NLS-1$
	private static final String ADD_DEFAULT_INCLUDE_TITLE = "New file included by default";//$NON-NLS-1$
	private static final String ADD_DEFAULT_INCLUDE_MESSAGE = "Enter a path of file that should be included to each source and header file.";//$NON-NLS-1$
	private static final String EDIT_DEFAULT_INCLUDE_TITLE = "Edit path of file included by default";//$NON-NLS-1$
	private static final String EDIT_DEFAULT_INCLUDE_MESSAGE = "Edit the path of file that should be included to each source and header file.";//$NON-NLS-1$

	private static final String PREDEFINED_MACROS_FIELD = "Predefined macros";//$NON-NLS-1$
	private static final String PREDEFINED_MACROS_TIP = "Predefined preprocessor macro (optionally with specified value).";//$NON-NLS-1$
	private static final String ADD_PREDEFINED_MACRO_TITLE = "New predefined macro";//$NON-NLS-1$
	private static final String ADD_PREDEFINED_MACRO_MESSAGE = "Enter a preprocessor macro (optionally with specified value).";//$NON-NLS-1$
	private static final String EDIT_PREDEFINED_MACRO_TITLE = "Edit predefined macro";//$NON-NLS-1$
	private static final String EDIT_PREDEFINED_MACRO_MESSAGE = "Edit the preprocessor macro (optionally with specified value).";//$NON-NLS-1$

	private final PageCompositeListener pageCompositeListener;

	private Composite container;
	private SingleStringField includePaths;
	private SingleStringField defaultIncludes;
	private SingleStringField predefinedMacros;

	public ProjectAdditionalPathSettingsComposite(Composite parent, PageCompositeListener pageCompositeListener) {
		super(parent, SWT.NONE);
		this.pageCompositeListener = pageCompositeListener;
		createControl(parent);
	}

	private void createControl(Composite parent) {
		container = new Composite(parent, NONE);
		GridData parentData = new GridData(FILL, FILL, true, true);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		container.setLayoutData(parentData);

		final Label tosdirInfo = new Label(container, NONE);
		tosdirInfo.setText(TOSDIR_VARIABLE);

		final GridData centerLayoutData = new GridData(FILL, FILL, true, true);

		includePaths = SingleStringField.builder()
				.parent(container, centerLayoutData, getShell())
				.fieldName("")
				.label(INCLUDE_PATHS_FIELD)
				.tip(INCLUDE_PATHS_TIP)
				.width(COLUMN_WIDTH)
				.addValueStrings(ADD_INCLUDE_PATH_TITLE, ADD_INCLUDE_PATH_MESSAGE)
				.editValueStrings(EDIT_INCLUDE_PATH_TITLE, EDIT_INCLUDE_PATH_MESSAGE)
				.build();

		defaultIncludes = SingleStringField.builder()
				.parent(container, centerLayoutData, getShell())
				.fieldName("")
				.label(DEFAULT_INCLUDES_FIELD)
				.tip(DEFAULT_INCLUDES_TIP)
				.width(COLUMN_WIDTH)
				.addValueStrings(ADD_DEFAULT_INCLUDE_TITLE, ADD_DEFAULT_INCLUDE_MESSAGE)
				.editValueStrings(EDIT_DEFAULT_INCLUDE_TITLE, EDIT_DEFAULT_INCLUDE_MESSAGE)
				.build();

		predefinedMacros = SingleStringField.builder()
				.parent(container, centerLayoutData, getShell())
				.fieldName("")
				.label(PREDEFINED_MACROS_FIELD)
				.tip(PREDEFINED_MACROS_TIP)
				.width(COLUMN_WIDTH)
				.addValueStrings(ADD_PREDEFINED_MACRO_TITLE, ADD_PREDEFINED_MACRO_MESSAGE)
				.editValueStrings(EDIT_PREDEFINED_MACRO_TITLE, EDIT_PREDEFINED_MACRO_MESSAGE)
				.build();

		includePaths.align(new AbstractField[] { defaultIncludes, predefinedMacros });
		pageCompositeListener.setPageComplete(validatePage());
	}

	public List<String> getAdditionalIncludePaths() {
		return transform(includePaths.getValue());
	}

	public List<String> getAdditionalDefaultIncludes() {
		return transform(defaultIncludes.getValue());
	}

	public List<String> getAdditionalPredefinedMacros() {
		return transform(predefinedMacros.getValue());
	}

	/**
	 * Inserts given values into displayed tables.
	 *
	 * @param paths
	 *            additional include paths
	 * @param files
	 *            additional default includes
	 * @param macros
	 *            additional predefined macros
	 */
	public void setData(List<String> paths, List<String> files, List<String> macros) {
		includePaths.setData(paths);
		defaultIncludes.setData(files);
		predefinedMacros.setData(macros);
	}

	private boolean validatePage() {
		return true;
	}

	private List<String> transform(List<StringValue> from) {
		return Lists.transform(from, new Function<StringValue, String>() {
			@Override
			public String apply(StringValue value) {
				return value.getName();
			}
		});
	}
}
