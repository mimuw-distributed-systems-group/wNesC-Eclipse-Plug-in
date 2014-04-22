package pl.edu.mimuw.nesc.plugin.wizards.fields;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import static org.eclipse.swt.SWT.*;

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
        createLabel(composite, fieldName);
    }

    @Override
    public final String getName() {
        return name;
    }

    protected Composite getComposite() {
        return composite;
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
     * @param fieldName Text in this label (a colon will be appended).
     * @return Label for this field.
     */
    private static Label createLabel(Composite parent, String fieldName) {
        final Label result = new Label(parent, NONE);
        result.setText(fieldName + ":");

        return result;
    }
}
