package pl.edu.mimuw.nesc.plugin.wizards.fields;

import static org.eclipse.swt.SWT.CENTER;
import static org.eclipse.swt.SWT.LEFT;
import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Abstract class that represents a wizard field. It creates a composite and
 * places a label in it.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public abstract class AbstractField implements WizardField {
    /**
     * Composite that contains all controls for this field. Never null.
     */
    private final Composite composite;

    /**
     * Label for this field.
     */
    private final Label label;

    /**
     * Name of the value that this field will allow to enter. Never null.
     */
    private final String name;

    /**
     * Creates a composite for this field.
     *
     * @param parent Parent for the composite for this field.
     * @param fieldName Name associated with this field.
     * @param columnsCount Number of columns in the grid layout that the
     *                     composite for this field will contain.
     * @param layoutData Layout data object to associate with the newly created
     *                   composite for this field.
     */
    protected AbstractField(Composite parent, String fieldName, int columnsCount, Object layoutData) {
        if (parent == null || fieldName == null) {
            throw new NullPointerException("AbstractField.<init>: null argument.");
        } else if (columnsCount < 1) {
            throw new IllegalArgumentException("AbstractField.<init>: number of columns must be positive.");
        }

        this.composite = createComposite(parent, columnsCount);
        if (layoutData != null) {
            this.composite.setLayoutData(layoutData);
        }

        this.name = fieldName;
        this.label = createLabel(composite, fieldName);
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public void setEnabled(boolean enabled) {
        composite.setEnabled(enabled);
        label.setEnabled(enabled);
    }

    protected Composite getComposite() {
        return composite;
    }

    /**
     * After this call this field and those from the given array are aligned,
     * i.e. their controls after label begin in the same position.
     *
     * @param fields Array with fields to be aligned with.
     * @throws NullPointerException Argument is null.
     * @throws IllegalArgumentException One of the elements of the given
     *                                  array is null or this.
     */
    public void align(AbstractField[] fields) {
       // Validate argument
        if (fields == null) {
            throw new NullPointerException("Fields array is null");
        }
        for (AbstractField field : fields) {
            if (field == this || field == null) {
                throw new IllegalArgumentException("One of the array elements is this field or null.");
            }
        }

        align(fields, 0, 0);
    }

    /**
     * Method that realizes the alignment.
     *
     * @return Maximum width of a label including this and fields from given
     *         array.
     */
    private int align(AbstractField[] fields, int nextIndex, int curMax) {
        // Compute the maximum width
        getComposite().pack();
        curMax = Math.max(curMax, label.getSize().x);
        if (nextIndex < fields.length) {
            curMax = Math.max(curMax, fields[nextIndex].align(fields, nextIndex + 1, curMax));
        }

        // Do the alignment
        final GridData layoutData = new GridData(LEFT, CENTER, false, false);
        layoutData.widthHint = curMax;
        label.setLayoutData(layoutData);

        return curMax;
    }

    /**
     * Creates and configures the composite of this field.
     *
     * @return The newly created and configured composite.
     */
    private static Composite createComposite(Composite parent, int columnsCount) {
        final Composite result = new Composite(parent, NONE);
        final GridLayout layout = new GridLayout();
        layout.numColumns = columnsCount;
        result.setLayout(layout);

        return result;
    }

    /**
     * Creates and returns a label for this field. It is created as a child of
     * the given composite.
     *
     * @param fieldName Text in this label (a colon will be appended if text
     * 					is not empty).
     * @return Label for this field.
     */
    private static Label createLabel(Composite parent, String fieldName) {
        final Label result = new Label(parent, NONE);
        if ("".equals(fieldName)) {
        	result.setText(fieldName);
        } else {
        	result.setText(fieldName + ":");
        }

        return result;
    }
}
