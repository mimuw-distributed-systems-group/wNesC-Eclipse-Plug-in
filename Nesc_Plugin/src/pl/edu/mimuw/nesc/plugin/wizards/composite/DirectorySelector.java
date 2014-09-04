package pl.edu.mimuw.nesc.plugin.wizards.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Optional;

/**
 * A composite which enables user to select any directory path from local file
 * system.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class DirectorySelector extends Composite {

	private static final String BROWSE = "Browse...";
	private static final String SELECT_DIRECTORY = "Select directory";

	private final Optional<String> additionalButtonLabel;
	private final Optional<String> additionalButtonLabel2;

	private Text pathText;
	private Button browseButton;
	private Button additionalButton;
	private Button additionalButton2;

	private String path;

	public DirectorySelector(Composite parent) {
		this(parent, Optional.<String> absent(), Optional.<String> absent());
	}

	public DirectorySelector(Composite parent, String additionalButtonLabel) {
		this(parent, Optional.of(additionalButtonLabel), Optional.<String> absent());
	}

	public DirectorySelector(Composite parent, String additionalButtonLabel, String additionalButtonLabel2) {
		this(parent, Optional.of(additionalButtonLabel), Optional.of(additionalButtonLabel2));
	}

	private DirectorySelector(Composite parent, Optional<String> additionalButtonLabel,
			Optional<String> additionalButtonLabel2) {
		super(parent, SWT.NONE);
		this.additionalButtonLabel = additionalButtonLabel;
		this.additionalButtonLabel2 = additionalButtonLabel2;

		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, false);
		int columns = 2 + (additionalButtonLabel.isPresent() ? 1 : 0) + (additionalButtonLabel2.isPresent() ? 1 : 0);
		GridLayout layout = new GridLayout(columns, false);
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);
		this.setLayoutData(parentData);
		createContent();
	}

	private void createContent() {
		/* Create the text box extra wide to show long paths. */
		pathText = new Text(this, SWT.BORDER | SWT.SINGLE);
		final GridData dataLayout = new GridData(SWT.FILL, SWT.FILL, true, false);
		pathText.setLayoutData(dataLayout);
		pathText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				path = pathText.getText();
			}
		});

		browseButton = new Button(this, SWT.PUSH);
		browseButton.setText(BROWSE);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				final DirectoryDialog dlg = new DirectoryDialog(getShell());
				/*
				 * Set the initial filter path according to anything they've
				 * selected or typed in.
				 */
				dlg.setFilterPath(pathText.getText());
				dlg.setText(SELECT_DIRECTORY);
				dlg.setMessage(SELECT_DIRECTORY);
				/*
				 * Calling open() will open and run the dialog. It will return
				 * the selected directory, or null if user cancels.
				 */
				final String dir = dlg.open();
				if (dir != null) {
					/* Set the text box to the new selection. */
					pathText.setText(dir);
					path = dir;
				}
			}
		});

		if (additionalButtonLabel.isPresent()) {
			additionalButton = new Button(this, SWT.PUSH);
			additionalButton.setText(additionalButtonLabel.get());
		}

		if (additionalButtonLabel2.isPresent()) {
			additionalButton2 = new Button(this, SWT.PUSH);
			additionalButton2.setText(additionalButtonLabel2.get());
		}
	}

	public Button getAdditionalButton() {
		return additionalButton;
	}

	public Button getAdditionalButton2() {
		return additionalButton2;
	}

	public String getSelectedPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
		pathText.setText(path);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		pathText.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	@Override
	public void addListener(int eventType, Listener listener) {
		super.addListener(eventType, listener);
		pathText.addListener(eventType, listener);
		browseButton.addListener(eventType, listener);
	}
}
