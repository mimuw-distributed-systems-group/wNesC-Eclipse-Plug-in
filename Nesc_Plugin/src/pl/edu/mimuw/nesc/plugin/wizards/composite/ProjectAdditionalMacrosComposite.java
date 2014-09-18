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
 * A composite for selecting additional files or macros. It is a main
 * part of both a new project wizard page and project property page.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class ProjectAdditionalMacrosComposite extends Composite {

	private static final int COLUMN_WIDTH = 400;
	private static final String TOSDIR_VARIABLE = "Use ${TOSDIR} variable to refer to TinyOS directory.";

	private static final String DEFAULT_INCLUDES_FIELD = "Files included by default";
	private static final String DEFAULT_INCLUDES_TIP = "File that should be included to each source and header file.";
	private static final String ADD_DEFAULT_INCLUDE_TITLE = "New file included by default";
	private static final String ADD_DEFAULT_INCLUDE_MESSAGE = "Enter a path of file that should be included to each source and header file.";
	private static final String EDIT_DEFAULT_INCLUDE_TITLE = "Edit path of file included by default";
	private static final String EDIT_DEFAULT_INCLUDE_MESSAGE = "Edit the path of file that should be included to each source and header file.";

	private static final String PREDEFINED_MACROS_FIELD = "Predefined macros";
	private static final String PREDEFINED_MACROS_TIP = "Predefined preprocessor macro (optionally with specified value).";
	private static final String ADD_PREDEFINED_MACRO_TITLE = "New predefined macro";
	private static final String ADD_PREDEFINED_MACRO_MESSAGE = "Enter a preprocessor macro (optionally with specified value).";
	private static final String EDIT_PREDEFINED_MACRO_TITLE = "Edit predefined macro";
	private static final String EDIT_PREDEFINED_MACRO_MESSAGE = "Edit the preprocessor macro (optionally with specified value).";

	private final PageCompositeListener pageCompositeListener;

	private SingleStringField defaultIncludes;
	private SingleStringField predefinedMacros;

	public ProjectAdditionalMacrosComposite(Composite parent, PageCompositeListener pageCompositeListener) {
		super(parent, SWT.NONE);
		this.pageCompositeListener = pageCompositeListener;
		createControl(parent);
	}

	private void createControl(Composite parent) {
		GridLayout layout = new GridLayout();
		GridData layoutData = new GridData(FILL, FILL, true, true);
		setLayout(layout);
		setLayoutData(layoutData);

		final Label tosdirInfo = new Label(this, NONE);
		tosdirInfo.setText(TOSDIR_VARIABLE);

		final GridData centerLayoutData = new GridData(FILL, FILL, true, true);

		defaultIncludes = SingleStringField.builder()
				.fieldName("")
				.label(DEFAULT_INCLUDES_FIELD)
				.tip(DEFAULT_INCLUDES_TIP)
				.width(COLUMN_WIDTH)
				.addValueStrings(ADD_DEFAULT_INCLUDE_TITLE, ADD_DEFAULT_INCLUDE_MESSAGE)
				.editValueStrings(EDIT_DEFAULT_INCLUDE_TITLE, EDIT_DEFAULT_INCLUDE_MESSAGE)
				.parentShell(getShell())
				.parentComposite(this)
				.layoutData(centerLayoutData)
				.build();

		predefinedMacros = SingleStringField.builder()
				.fieldName("")
				.label(PREDEFINED_MACROS_FIELD)
				.tip(PREDEFINED_MACROS_TIP)
				.width(COLUMN_WIDTH)
				.addValueStrings(ADD_PREDEFINED_MACRO_TITLE, ADD_PREDEFINED_MACRO_MESSAGE)
				.editValueStrings(EDIT_PREDEFINED_MACRO_TITLE, EDIT_PREDEFINED_MACRO_MESSAGE)
				.parentShell(getShell())
				.parentComposite(this)
				.layoutData(centerLayoutData)
				.build();

		defaultIncludes.align(new AbstractField[] { predefinedMacros });
		pageCompositeListener.setPageComplete(validatePage());
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
	 * @param files
	 *            additional default includes
	 * @param macros
	 *            additional predefined macros
	 */
	public void setData(List<String> files, List<String> macros) {
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
