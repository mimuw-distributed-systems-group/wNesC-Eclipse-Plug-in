package pl.edu.mimuw.nesc.plugin.wizards.fields;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.swt.SWT.CENTER;
import static org.eclipse.swt.SWT.LEFT;
import static org.eclipse.swt.SWT.NONE;

import com.google.common.base.Optional;
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
     * @param builder Builder for this class.
     */
    protected AbstractField(Builder<? extends AbstractField> builder) {
        this.composite = builder.buildComposite();
        this.name = builder.fieldName;
        this.label = builder.buildLabel(composite);
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
     * <p>The base class for builders of fields. The process of building the
     * resulting object is as follows:</p>
     * <ol>
     *    <li><code>beforeBuild</code> method is called</li>
     *    <li><code>validate</code> method is called</li>
     *    <li><code>create</code> method is called</li>
     * </ol>
     * <p>The result of the <code>create</code> method call is considered the
     * built object and returned by <code>build</code> method.</p>
     * <p>The design of this class follows the builder pattern.</p>
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     * @see Builder#beforeBuild
     * @see Builder#validate
     */
    public static abstract class Builder<F extends AbstractField> {
        /**
         * <code>true</code> if and only if the field name has been already set.
         */
        private boolean fieldNameSet = false;

        /**
         * Data for the creation of the field.
         */
        private final int columnsCount;
        private Composite parentComposite;
        private String fieldName;
        private Optional<Object> layoutData = Optional.absent();

        /**
         * @param columnsCount Number of columns in the grid layout that the
         *                     composite for the field will contain.
         */
        protected Builder(int columnsCount) {
            this.columnsCount = columnsCount;
        }

        /**
         * Set the parent of the composite for the field.
         *
         * @param parent Parent for the composite for the field.
         * @return <code>this</code>
         */
        public Builder<F> parentComposite(Composite parent) {
            this.parentComposite = parent;
            return this;
        }

        /**
         * Set the layout data object that will be associated with the new
         * composite for the field.
         *
         * @param layoutData Layout data object to associate with the newly
         *                   created composite for the field. If it is null,
         *                   then no layout data object will be used.
         * @return <code>this</code>
         */
        public Builder<F> layoutData(Object layoutData) {
            this.layoutData = Optional.fromNullable(layoutData);
            return this;
        }

        /**
         * This method should be called in a <code>beforeBuild</code> override.
         *
         * @param fieldName Name to be associated with the field.
         * @throws IllegalStateException The name of the field has been
         *                               already set.
         */
        protected void setFieldName(String fieldName) {
            checkState(!fieldNameSet, "the name of the field can be set exactly once");
            this.fieldName = fieldName;
            this.fieldNameSet = true;
        }

        /**
         * This method is called directly before the building process begins and
         * can make some additional work. It should make a call to the same
         * method from the superclass.
         */
        protected void beforeBuild() {
        }

        /**
         * Checks if the fields of this builder are correctly set. If no, an
         * exception should be thrown. This method should make a call to the
         * same method from the superclass.
         */
        protected void validate() {
            checkNotNull(parentComposite, "the parent composite cannot be null");
            checkNotNull(fieldName, "the field name cannot be null");
            checkState(columnsCount >= 1, "the number of columns must be positive");
        }

        /**
         * This method is called at the very end of the building process. It
         * should cause creation of the object.
         *
         * @return Instance that has been built by this builder.
         */
        protected abstract F create();

        /**
         * Performs the building work. An exception can be thrown if the builder
         * has been incorrectly configured.
         *
         * @return The instance created by this builder.
         */
        public final F build() {
            beforeBuild();
            validate();
            return create();
        }

        /**
         * Creates and configures the composite for the field.
         *
         * @return The newly created and configured composite.
         */
        private Composite buildComposite() {
            final Composite result = new Composite(parentComposite, NONE);
            final GridLayout layout = new GridLayout();
            layout.numColumns = columnsCount;
            result.setLayout(layout);

            if (layoutData.isPresent()) {
                result.setLayoutData(layoutData.get());
            }

            return result;
        }

        /**
         * Creates and returns a label for the field. It is created as a child
         * of the given composite.
         *
         * @param parent Composite that will be used as the parent for the
         *               created label.
         * @return Label for the field.
         */
        private Label buildLabel(Composite parent) {
            final Label result = new Label(parent, NONE);
            if ("".equals(fieldName)) {
                    result.setText(fieldName);
            } else {
                    result.setText(fieldName + ":");
            }

            return result;
        }
    }
}
