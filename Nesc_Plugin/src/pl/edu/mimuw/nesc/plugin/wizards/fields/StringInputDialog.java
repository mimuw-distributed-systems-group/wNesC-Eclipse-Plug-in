package pl.edu.mimuw.nesc.plugin.wizards.fields;

import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.CENTER;
import static org.eclipse.swt.SWT.FILL;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.SINGLE;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import pl.edu.mimuw.nesc.plugin.wizards.fields.SingleStringField.StringValue;

/**
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class StringInputDialog extends Dialog {

	private static final int MINIMUM_WIDTH = 300;
	private static final int MINIMUM_HEIGHT = 50;

	private final String title;

	private Group inputStringGroup;
	private Text inputStringText;

	private StringValue value;

	StringInputDialog(Shell parent, String title) {
		this(parent, title, null);
	}

	StringInputDialog(Shell parent, String title, StringValue data) {
		super(parent);
		this.title = title;
		this.value = data;
	}

    @Override
    public int open() {
        create();
        setInitialValues();
        return super.open();
    }

	@Override
	protected Composite createDialogArea(Composite parent) {
		final Composite dialogArea = (Composite) super.createDialogArea(parent);
		final GridData dialogAreaLayoutData = new GridData(FILL, FILL, true, true);
		dialogAreaLayoutData.minimumHeight = MINIMUM_HEIGHT;
		dialogAreaLayoutData.minimumWidth = MINIMUM_WIDTH;
		dialogArea.setLayoutData(dialogAreaLayoutData);
		dialogArea.setLayout(new GridLayout());

		// Prepare layout objects
		final GridData groupLayoutData = new GridData(FILL, CENTER, true, true);
		final GridLayout groupLayout = new GridLayout();
		final GridData layoutData = new GridData(FILL, CENTER, true, true);

		inputStringGroup = new Group(dialogArea, NONE);
		inputStringGroup.setLayoutData(groupLayoutData);
		inputStringGroup.setLayout(groupLayout);
		inputStringGroup.setText(title);

		// Controls for typing input string
		inputStringText = new Text(inputStringGroup, SINGLE | BORDER);
		inputStringText.setSize(1000, inputStringText.getSize().y);
		inputStringText.setLayoutData(layoutData);

		return dialogArea;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	protected void okPressed() {
		this.value = new StringValue(inputStringText.getText());
		super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		this.value = new StringValue(inputStringText.getText());
		super.cancelPressed();
	}

	private void setInitialValues() {
		if (this.value != null) {
			setData(this.value);
		}
	}

	void insertData(StringValue value) {
		if (this.value == null) {
			return;
		}
		value.setName(this.value.getName());
	}

	StringValue getData() {
		return this.value;
	}

	private void setData(StringValue value) {
		inputStringText.setText(value.getName());
	}

}
