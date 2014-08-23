package pl.edu.mimuw.nesc.plugin.wizards.fields;

import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import pl.edu.mimuw.nesc.plugin.wizards.fields.SingleStringField.StringValue;

/**
 * A field that allows user to specify a list of string values, e.g. file paths.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class SingleStringField extends TableField<StringValue> {

	private static final String ERROR_MSG_EMPTY_VALUE = "Value cannot be empty.";

	public static Builder builder() {
		return new Builder();
	}

	private final Shell shell;
	private final String addValueTitle;
	private final String editValueTitle;
	private final String addValueMessage;
	private final String editValueMessage;

	private SingleStringField(Builder builder) {
		super(builder.parent, builder.fieldName, builder.layoutData,
				new ColumnSpecification[] { new ColumnSpecification(builder.label, builder.width, builder.tip) });
		this.shell = builder.shell;
		this.addValueTitle = builder.addValueTitle;
		this.addValueMessage = builder.addValueMessage;
		this.editValueTitle = builder.editValueTitle;
		this.editValueMessage = builder.editValueMessage;
	}

	/**
	 * Initialize tablie with given items.
	 *
	 * @param items
	 *            initial values
	 */
	public void setData(List<String> items) {
		for (String item : items) {
			final StringValue value = new StringValue(item);
			final TableItem newItem = newItem(value);
			newItem.setText(value.getName());
		}
	}

	@Override
	public String getErrorStatus() {
		// Value in this field is always valid
		return null;
	}

	@Override
	protected void addItemOperation() {
		final InputDialog dialog = new InputDialog(shell, addValueTitle, addValueMessage, "", new StringValidator());

		dialog.open();
		final String str = dialog.getValue();
		if (str != null && !dialog.getValue().isEmpty()) {
			final StringValue value = new StringValue(str);
			final TableItem newItem = newItem(value);
			newItem.setText(value.getName());
		}
	}

	@Override
	protected void editItemOperation(TableItem item, StringValue data) {
		final InputDialog dialog = new InputDialog(shell, editValueTitle, editValueMessage, data.getName(),
				new StringValidator());
		dialog.open();

		final String str = dialog.getValue();
		if (str != null && !dialog.getValue().isEmpty()) {
			item.setText(str);
		}
	}

	/**
	 * Wrapper for string.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	public static class StringValue {

		private String name;

		public StringValue(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	/**
	 * Class of objects that checks whether a string value is valid (non-null
	 * and not empty).
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	private class StringValidator implements IInputValidator {

		@Override
		public String isValid(String text) {
			if (text == null || text.isEmpty()) {
				return ERROR_MSG_EMPTY_VALUE;
			}
			return null;
		}
	}

	/**
	 * String field builder.
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	public static class Builder {

		private Composite parent;
		private Shell shell;
		private String fieldName;
		private Object layoutData;
		private String label;
		private int width;
		private String tip;

		private String addValueTitle;
		private String editValueTitle;
		private String addValueMessage;
		private String editValueMessage;

		public Builder() {
		}

		public Builder parent(Composite parent, Object layoutData, Shell parentShell) {
			this.parent = parent;
			this.layoutData = layoutData;
			this.shell = parentShell;
			return this;
		}

		public Builder width(int width) {
			this.width = width;
			return this;
		}

		public Builder fieldName(String fieldName) {
			this.fieldName = fieldName;
			return this;
		}

		public Builder label(String label) {
			this.label = label;
			return this;
		}

		public Builder tip(String tip) {
			this.tip = tip;
			return this;
		}

		public Builder addValueStrings(String dialogTitle, String dialogMessage) {
			this.addValueTitle = dialogTitle;
			this.addValueMessage = dialogMessage;
			return this;
		}

		public Builder editValueStrings(String dialogTitle, String dialogMessage) {
			this.editValueTitle = dialogTitle;
			this.editValueMessage = dialogMessage;
			return this;
		}

		public SingleStringField build() {
			verify();
			return new SingleStringField(this);
		}

		private void verify() {
			// TODO Auto-generated method stub
		}
	}
}
