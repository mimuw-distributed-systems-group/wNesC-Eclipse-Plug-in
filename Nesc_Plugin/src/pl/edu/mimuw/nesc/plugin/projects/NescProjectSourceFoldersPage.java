package pl.edu.mimuw.nesc.plugin.projects;

import static org.eclipse.swt.SWT.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.osgi.service.prefs.BackingStoreException;

import pl.edu.mimuw.nesc.plugin.projects.util.PathsUtil;
import pl.edu.mimuw.nesc.plugin.projects.util.PathsUtil.Path;
import pl.edu.mimuw.nesc.plugin.wizards.composite.DirectorySelector;

/**
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescProjectSourceFoldersPage extends NescPropertyPage {

	private static final String ERROR_MSG_EMPTY_VALUE = "Empty value.";
	private static final String TABLE_ITEM_PROPERTY_CUSTOM_DATA = "custom_data";

	private Table table;

	private Button topButton;
	private Button upButton;
	private Button downButton;
	private Button bottomButton;
	private Button addButton;
	private Button removeButton;
	private Button selectRecursivelyButton;
	private Button deselectRecursivelyButton;
	private Button selectAllButton;
	private Button deselectAllButton;
	private DirectorySelector directorySelector;

	@Override
	protected Control createContents(Composite parent) {
		final IProject project = getProject();
		if (project == null) {
			return parent;
		}

		final Composite baseContainer = new Composite(parent, NONE);
		final GridLayout baseGridLayout = new GridLayout();
		final GridData baseGridData = new GridData(FILL, FILL, true, true);
		baseContainer.setLayout(baseGridLayout);
		baseContainer.setLayoutData(baseGridData);

		final Composite container = new Composite(baseContainer, NONE);
		final GridLayout gridLayout = new GridLayout(2, false);
		final GridData gridData = new GridData(FILL, FILL, true, true);
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		initializeTables(container);
		initializeButtons(container);

		directorySelector = new DirectorySelector(baseContainer, "Add");
		directorySelector.getAdditionalButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String str = directorySelector.getSelectedPath();
				if (str.isEmpty()) {
					setErrorMessage(ERROR_MSG_EMPTY_VALUE);
					return;
				}
				final Path path = new Path(str, str, true, true);
				final TableItem tableItem = new TableItem(table, NONE);
				tableItem.setText(str);
				tableItem.setData(TABLE_ITEM_PROPERTY_CUSTOM_DATA, path);
				directorySelector.setPath("");
			}
		});

		initializeValues();
		return new Composite(parent, NULL);
	}

	private void initializeTables(Composite parent) {
		final Composite tableContainer = new Composite(parent, NONE);
		final FillLayout tableLayout = new FillLayout();
		// FIXME: force table to take reasonable space regardless its content
		// size
		final GridData tableLayoutData = new GridData(475, 370);
		tableContainer.setLayout(tableLayout);
		tableContainer.setLayoutData(tableLayoutData);

		table = new Table(tableContainer, CHECK | BORDER | V_SCROLL | H_SCROLL | MULTI);
		table.setBounds(tableContainer.getClientArea());

		table.addListener(Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
			}
		});
	}

	private void initializeButtons(Composite parent) {
		final Composite buttonsContainer = new Composite(parent, NONE);
		final GridLayout buttonsLayout = new GridLayout();
		final GridData buttonsLayoutData = new GridData(FILL, FILL, true, true);
		buttonsContainer.setLayout(buttonsLayout);
		buttonsContainer.setLayoutData(buttonsLayoutData);

		final GridData buttonLayoutData = new GridData(FILL, NONE, true, false);

		topButton = new Button(buttonsContainer, NONE);
		topButton.setText("Top");
		topButton.setLayoutData(buttonLayoutData);
		upButton = new Button(buttonsContainer, NONE);
		upButton.setText("Up");
		upButton.setLayoutData(buttonLayoutData);

		downButton = new Button(buttonsContainer, NONE);
		downButton.setText("Down");
		downButton.setLayoutData(buttonLayoutData);
		bottomButton = new Button(buttonsContainer, NONE);
		bottomButton.setText("Bottom");
		bottomButton.setLayoutData(buttonLayoutData);

		/* Horizontal separator. */
		final Label separator = new Label(buttonsContainer, SEPARATOR | SHADOW_OUT | HORIZONTAL);
		separator.setLayoutData(new GridData(CENTER, NONE, true, false));

		addButton = new Button(buttonsContainer, NONE);
		addButton.setText("Add");
		addButton.setLayoutData(buttonLayoutData);
		removeButton = new Button(buttonsContainer, NONE);
		removeButton.setText("Remove");
		removeButton.setLayoutData(buttonLayoutData);

		/* Horizontal separator. */
		final Label separator2 = new Label(buttonsContainer, SEPARATOR | SHADOW_OUT | HORIZONTAL);
		separator2.setLayoutData(new GridData(CENTER, NONE, true, false));

		selectRecursivelyButton = new Button(buttonsContainer, NONE);
		selectRecursivelyButton.setText("Select recursively");
		selectRecursivelyButton.setLayoutData(buttonLayoutData);
		deselectRecursivelyButton = new Button(buttonsContainer, NONE);
		deselectRecursivelyButton.setText("Deselect recursively");
		deselectRecursivelyButton.setLayoutData(buttonLayoutData);

		/* Horizontal separator. */
		final Label separator3 = new Label(buttonsContainer, SEPARATOR | SHADOW_OUT | HORIZONTAL);
		separator3.setLayoutData(new GridData(CENTER, NONE, true, false));

		selectAllButton = new Button(buttonsContainer, NONE);
		selectAllButton.setText("Select all");
		selectAllButton.setLayoutData(buttonLayoutData);
		deselectAllButton = new Button(buttonsContainer, NONE);
		deselectAllButton.setText("Deselect all");
		deselectAllButton.setLayoutData(buttonLayoutData);

		topButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableItem[] selection = table.getSelection();
				if (selection.length == 0) {
					return;
				}

				for (int i = 0; i < selection.length; ++i) {
					moveTableItem(selection[i], i);
				}
				table.setSelection(0, selection.length - 1);
			}
		});

		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int[] selection = table.getSelectionIndices();
				if (selection.length == 0 || selection[0] == 0) {
					return;
				}

				for (int i = 0; i < selection.length; ++i) {
					moveTableItem(table.getItem(selection[i]), selection[i] - 1);
				}
				final int[] newSelection = new int[selection.length];
				for (int i = 0; i < newSelection.length; ++i) {
					newSelection[i] = selection[i] - 1;
				}
				table.setSelection(newSelection);
			}
		});

		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int itemsCount = table.getItemCount();
				final int[] selection = table.getSelectionIndices();
				if (selection.length == 0 || selection[selection.length - 1] == itemsCount - 1) {
					return;
				}

				for (int i = selection.length - 1; i >= 0; --i) {
					moveTableItem(table.getItem(selection[i]), selection[i] + 2);
				}
				final int[] newSelection = new int[selection.length];
				for (int i = 0; i < newSelection.length; ++i) {
					newSelection[i] = selection[i] + 1;
				}
				table.setSelection(newSelection);
			}
		});

		bottomButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int itemsCount = table.getItemCount();
				final int[] selection = table.getSelectionIndices();

				if (selection.length == 0 || selection[selection.length - 1] == itemsCount - 1) {
					return;
				}

				for (int i = 0; i < selection.length; ++i) {
					moveTableItem(table.getItem(selection[i] - i), itemsCount);
				}
				table.setSelection(itemsCount - selection.length, itemsCount - 1);
			}
		});

		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final InputDialog dialog = new InputDialog(getShell(), "", "", "", new StringValidator());

				dialog.open();
				final String str = dialog.getValue();
				if (str != null && !dialog.getValue().isEmpty()) {
					final Path path = new Path(str, str, true, true);
					final TableItem tableItem = new TableItem(table, NONE);
					tableItem.setText(str);
					tableItem.setData(TABLE_ITEM_PROPERTY_CUSTOM_DATA, path);
				}
			}
		});

		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableItem[] selection = table.getSelection();
				for (TableItem item : selection) {
					final Path data = (Path) item.getData(TABLE_ITEM_PROPERTY_CUSTOM_DATA);
					if (data.isCustom()) {
						item.dispose();
					}
				}
			}
		});

		selectRecursivelyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final List<TableItem> items = getAffectedSubdirs();
				for (TableItem item : items) {
					item.setChecked(true);
				}
			}
		});

		deselectRecursivelyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final List<TableItem> items = getAffectedSubdirs();
				for (TableItem item : items) {
					item.setChecked(false);
				}
			}
		});

		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableItem[] items = table.getItems();
				for (TableItem item : items) {
					item.setChecked(true);
				}
			}
		});

		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableItem[] items = table.getItems();
				for (TableItem item : items) {
					item.setChecked(false);
				}
			}
		});
	}

	private void moveTableItem(TableItem tableItem, int indexTo) {
		final TableItem newItem = new TableItem(table, NONE, indexTo);
		newItem.setText(tableItem.getText());
		newItem.setChecked(tableItem.getChecked());
		newItem.setData(TABLE_ITEM_PROPERTY_CUSTOM_DATA, tableItem.getData(TABLE_ITEM_PROPERTY_CUSTOM_DATA));
		tableItem.dispose();
	}

	private List<TableItem> getAffectedSubdirs() {
		final TableItem[] items = table.getItems();
		final TableItem[] selection = table.getSelection();
		final List<TableItem> result = new ArrayList<>();

		final Set<String> selectedPaths = new HashSet<>();
		for (TableItem item : selection) {
			selectedPaths.add(((Path) item.getData(TABLE_ITEM_PROPERTY_CUSTOM_DATA)).getValue());
		}

		for (TableItem item : items) {
			final String itemValue = ((Path) item.getData(TABLE_ITEM_PROPERTY_CUSTOM_DATA)).getValue();
			for (String path : selectedPaths) {
				if (itemValue.startsWith(path)) {
					result.add(item);
					break;
				}
			}
		}
		return result;
	}

	@Override
	protected void initializeDefaults() {
		table.clearAll();
		loadValues();
	}

	@Override
	protected void initializeValues() {
		table.clearAll();
		loadValues();
	}

	@Override
	protected void storeValues() {
		final TableItem[] items = table.getItems();
		final List<Path> paths = new ArrayList<>();

		for (TableItem item : items) {
			final Path oldPath = (Path) item.getData(TABLE_ITEM_PROPERTY_CUSTOM_DATA);
			final Path newPath = new Path(oldPath.getDisplayString(), oldPath.getValue(), item.getChecked(),
					oldPath.isCustom());
			paths.add(newPath);
		}

		try {
			PathsUtil.saveNonPlatformPaths(getProject(), paths);
		} catch (BackingStoreException e) {
			e.printStackTrace();
			setErrorMessage("Cannot save settings. Backing store exception.");
		}
	}

	private void loadValues() {
		final List<Path> directories = PathsUtil.getNonPlatformPaths(getProject());

		for (Path dir : directories) {
			final TableItem tableItem = new TableItem(table, NONE);
			tableItem.setText(dir.getDisplayString());
			tableItem.setChecked(dir.isActive());
			tableItem.setData(TABLE_ITEM_PROPERTY_CUSTOM_DATA, dir);
		}
	}

	/**
	 * Class of objects that checks whether a string value is valid (non-null
	 * and not empty).
	 *
	 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
	 *
	 */
	private static class StringValidator implements IInputValidator {

		@Override
		public String isValid(String text) {
			if (text == null || text.isEmpty()) {
				return ERROR_MSG_EMPTY_VALUE;
			}
			return null;
		}
	}
}
