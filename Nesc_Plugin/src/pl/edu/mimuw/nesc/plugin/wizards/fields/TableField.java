package pl.edu.mimuw.nesc.plugin.wizards.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import static org.eclipse.swt.SWT.*;

/**
 * Class that represents a wizard field that allows the user to specify a list
 * of values with some properties. The list can be thought as a table. The
 * properties are represented by columns in that table. This field uses the
 * Table control to visualize the data from the table for the user.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 *
 * @param <T> Objects of this type are responsible of representing data in each
 *            row in the table. List of such objects is returned by
 *            <code>getValue</code> method.
 */

public abstract class TableField<T> extends AbstractField {
    /**
     * Number of columns in the grid layout for this field.
     */
    private static final int FIELD_COLUMNS_COUNT = 3;

    /**
     * Labels used for buttons.
     */
    private static final String LABEL_ADD = "Add...";
    private static final String LABEL_EDIT = "Edit...";
    private static final String LABEL_REMOVE = "Remove";

    /**
     * The table control used to visualize the data for the user.
     */
    private final Table table;

    /**
     * Buttons that are shown in this field.
     */
    private final ButtonPack buttons;

    /**
     * List with all data that is visualized in the Table control for the user.
     * There exists a bijection from the elements of the list and rows of the
     * table control.
     */
    private final List<T> data = new ArrayList<>();

    /**
     * Creates all controls for this field.
     *
     * @param parent Composite that will contain controls of this field.
     * @param fieldName Name of the field.
     * @param layoutData Object that affects layout of the composite created for
     *                   this field.
     * @param columns Array with specifications for all columns that will be
     *                created.
     * @throws NullPointerException One of the arguments (except
     *                              <code>layoutData</code>) is null.
     * @throws IllegalArgumentException The columns labels array has length
     *                                  lesser than one.
     */
    protected TableField(Composite parent, String fieldName, Object layoutData,
            ColumnSpecification[] columnsSpec) {
        super(parent, fieldName, FIELD_COLUMNS_COUNT, layoutData);

        // Validate the arguments
        if (columnsSpec == null) {
            throw new NullPointerException("Columns specification is null.");
        } else if (columnsSpec.length < 1) {
            throw new IllegalArgumentException("Columns specification is an empty array.");
        }

        table = createTable(getComposite(), columnsSpec);
        buttons = createButtons();
        createEnableListener();
    }

    @Override
    public final void setFocus() {
        table.forceFocus();
    }

    @Override
    public final List<T> getValue() {
        return Collections.unmodifiableList(data);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        table.setEnabled(enabled);
        buttons.addButton.setEnabled(enabled);

        if (enabled) {
            updateButtonsAccessibility();
        } else {
            buttons.editButton.setEnabled(false);
            buttons.removeButton.setEnabled(false);
        }
    }

    /**
     * Adds new item to the table.
     *
     * @param data Data object associated with the new item in the table.
     *             Must not be null.
     * @return Newly created table item object for the added data.
     * @throws NullPointerException The argument is null.
     */
    protected TableItem newItem(T itemData) {
        if (data == null) {
            throw new NullPointerException("Null argument");
        }

        data.add(itemData);
        return new TableItem(table, NONE);
    }

    /**
     * Called when the user requests new data to add.
     */
    protected abstract void addItemOperation();

    /**
     * Called when the user requests editing an existing entry. All editing
     * must be done by the subclass.
     *
     * @param item Table entry to be edited.
     * @param data Data associated with the edited entry.
     */
    protected abstract void editItemOperation(TableItem item, T data);

    /**
     * Creates and returns a new table object for this field. Its parent is set
     * to <code>parent</code> argument.
     *
     * @param parent Parent of the newly created object.
     * @param columnsLabels Array that specifies the number of columns in the
     *                      created table and their labels.
     * @return Newly created table object.
     */
    private static Table createTable(Composite parent, ColumnSpecification[] columnsSpec) {
        final Table result = new Table(parent, BORDER | SINGLE | FULL_SELECTION);
        result.setLayoutData(new GridData(FILL, FILL, true, true));
        result.setHeaderVisible(true);
        result.setLinesVisible(true);

        // Create all columns
        for (ColumnSpecification colSpec : columnsSpec) {
            final TableColumn column = new TableColumn(result, CENTER);

            column.setText(colSpec.label);
            if (colSpec.width > 0) {
                column.setWidth(colSpec.width);
            }
            if (colSpec.tipText != null) {
                column.setToolTipText(colSpec.tipText);
            }
        }

        return result;
    }

    /**
     * Creates buttons to let the user interact with the list and events for
     * them.
     */
    private ButtonPack createButtons() {
        // Create composite for buttons
        final Composite buttonsComposite = new Composite(getComposite(), NONE);
        buttonsComposite.setLayout(new FillLayout(VERTICAL));

        // Create buttons
        final Button addButton = new Button(buttonsComposite, NONE),
                     editButton = new Button(buttonsComposite, NONE),
                     removeButton = new Button(buttonsComposite, NONE);
        addButton.setText(LABEL_ADD);
        editButton.setText(LABEL_EDIT);
        removeButton.setText(LABEL_REMOVE);
        removeButton.setEnabled(false);
        editButton.setEnabled(false);

        // Add events for buttons
        addButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                addItemOperation();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
        editButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                final int selectedIndex = table.getSelectionIndex();
                if (selectedIndex != -1) {
                    editItemOperation(table.getItem(selectedIndex), data.get(selectedIndex));
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
        removeButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                final int selectedIndex = table.getSelectionIndex();
                if (selectedIndex != -1) {
                    table.remove(selectedIndex);
                    data.remove(selectedIndex);
                    removeButton.setEnabled(false);
                    editButton.setEnabled(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });

        return new ButtonPack(addButton, editButton, removeButton);
    }

    /**
     * Creates a listener of the table to properly enable buttons from the pack.
     *
     * @param pack Pack with buttons for the field.
     */
    private void createEnableListener() {
        table.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonsAccessibility();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });
    }

    /**
     * Set the accessibility of edit and remove buttons properly.
     */
    private void updateButtonsAccessibility() {
        final boolean existsSelected = table.getSelectionCount() > 0;
        buttons.editButton.setEnabled(existsSelected);
        buttons.removeButton.setEnabled(existsSelected);
    }

    /**
     * Class that represents the specification for a column that will be created
     * in the table control for this field.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    protected static class ColumnSpecification {
        /**
         * Label for the column. Never null.
         */
        private final String label;

        /**
         * Width of the column. May be non-positive. In such case it means
         * that the width will not be set.
         */
        private final int width;

        /**
         * Tip for the column. May be null. In such case it means that no tip
         * will be set.
         */
        private final String tipText;

        /**
         * Initializes fields of this object with given values.
         *
         * @param label Label for the column. Must not be null.
         * @param width Width for the column. Non-positive value is admissible
         *              and means an indeterminate value.
         * @param tipText Tip for the column. Null value is admissible and means
         *                an indeterminate value.
         * @throws NullPointerException The label is null.
         */
        public ColumnSpecification(String label, int width, String tipText) {
            if (label == null) {
                throw new NullPointerException("Label cannot be null in a label specification.");
            }

            this.label = label;
            this.width = width;
            this.tipText = tipText;
        }
    }

    /**
     * Class that groups the buttons created for this field.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    private static class ButtonPack {
        /**
         * Buttons that are created for this field.
         */
        private final Button addButton;
        private final Button editButton;
        private final Button removeButton;

        /**
         * Saves the given arguments inside this object.
         */
        private ButtonPack(Button addButton, Button editButton, Button removeButton) {
            this.addButton = addButton;
            this.editButton = editButton;
            this.removeButton = removeButton;
        }
    }
}
