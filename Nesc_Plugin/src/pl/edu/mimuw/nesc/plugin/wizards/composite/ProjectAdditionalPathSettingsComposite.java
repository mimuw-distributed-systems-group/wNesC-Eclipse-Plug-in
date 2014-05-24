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
	private static final String INLUDE_PATHS_TIP = "Directory to be searched for source and header files.";//$NON-NLS-1$
	private static final String DEFAULT_INCLUDES_FIELD = "Default includes";//$NON-NLS-1$
	private static final String DEFAULT_INCLUDES_TIP = "File to be included into each source and header file.";//$NON-NLS-1$
	private static final String PREDEFINED_MACROS_FIELD = "Predefined macros";//$NON-NLS-1$
	private static final String PREDEFINED_MACROS_TIP = "Predefined preprocessor macro (optionally with specified value).";//$NON-NLS-1$

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
		includePaths = new SingleStringField(container, getShell(), "", centerLayoutData, INCLUDE_PATHS_FIELD,
				COLUMN_WIDTH, INLUDE_PATHS_TIP);
		defaultIncludes = new SingleStringField(container, getShell(), "", centerLayoutData, DEFAULT_INCLUDES_FIELD,
				COLUMN_WIDTH, DEFAULT_INCLUDES_TIP);
		predefinedMacros = new SingleStringField(container, getShell(), "", centerLayoutData, PREDEFINED_MACROS_FIELD,
				COLUMN_WIDTH, PREDEFINED_MACROS_TIP);

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
