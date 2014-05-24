package pl.edu.mimuw.nesc.plugin.wizards.fields;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import pl.edu.mimuw.nesc.plugin.wizards.fields.SingleStringField.StringValue;

/**
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class SingleStringField extends TableField<StringValue> {

	/**
	 * Shell that will be used as the parent for dialogs. Never null.
	 */
	private final Shell shell;

	public SingleStringField(Composite parent, Shell parentShell, String fieldName, Object layoutData,
			String label, int width, String tip) {
		super(parent, fieldName, layoutData, new ColumnSpecification[] { new ColumnSpecification(label, width, tip) });
		checkArgument(parentShell != null, "Parent shell is null."); //$NON-NLS-1$
		this.shell = parentShell;
	}

	/**
	 *
	 * @param items
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
		// FIXME title
		final StringInputDialog dialog = new StringInputDialog(shell, "");

		if (dialog.open() == Dialog.OK) {
			final StringValue value = dialog.getData();
			final TableItem newItem = newItem(value);
			newItem.setText(value.getName());
		}
	}

	@Override
	protected void editItemOperation(TableItem item, StringValue data) {
		// FIXME title
		final StringInputDialog dialog = new StringInputDialog(shell, "", data);

		if (dialog.open() == Dialog.OK) {
			dialog.insertData(data);
			item.setText(data.getName());
		}
	}

	/**
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

}
