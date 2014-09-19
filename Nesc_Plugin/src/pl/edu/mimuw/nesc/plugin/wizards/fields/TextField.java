package pl.edu.mimuw.nesc.plugin.wizards.fields;

import com.google.common.base.Optional;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static org.eclipse.swt.SWT.*;

/**
 * Class that represents a text field - a field with only a label and a text
 * controls.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public abstract class TextField extends AbstractField {
    /**
     * Number of columns in the grid layout for this field.
     */
    private static final int FIELD_COLUMNS_COUNT = 2;

    /**
     * The text control for this field.
     */
    private final Text text;

    /**
     * Creates all controls for this field.
     *
     * @param builder Builder for this class.
     */
    protected TextField(Builder<? extends TextField> builder) {
        super(builder);
        this.text = builder.buildText(getComposite());

        if (builder.modifyListener.isPresent()) {
            addModifyListener(builder.modifyListener.get());
        }
    }

    @Override
    public final void setFocus() {
        text.forceFocus();
    }

    @Override
    public final String getValue() {
        return text.getText();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        text.setEnabled(enabled);
    }

    /**
     * Adds a modify listener that will be notified when the value in this field
     * changed.
     *
     * @param listener Listener to add.
     */
    public void addModifyListener(ModifyListener listener) {
        text.addModifyListener(listener);
    }

    /**
     * Builder for this part of the fields hierarchy.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public static abstract class Builder<F extends TextField> extends AbstractField.Builder<F> {
        /**
         * Data for creation of the field.
         */
        private Optional<ModifyListener> modifyListener = Optional.absent();

        /**
         * Initialize the superclass properly.
         */
        protected Builder() {
            super(FIELD_COLUMNS_COUNT);
        }

        /**
         * Set the optional initial modify listener.
         *
         * @param modifyListener Modify listener to set. Can be null - then no
         *                       modify listener will be set.
         * @return <code>this</code>
         */
        public Builder<F> modifyListener(ModifyListener modifyListener) {
            this.modifyListener = Optional.fromNullable(modifyListener);
            return this;
        }

        /**
         * Creates and returns a text control that is created as the child of the
         * given parent.
         *
         * @param parent Composite that will be the parent for the returned text
         *               control.
         * @return Newly created Text control.
         */
        private Text buildText(Composite parent) {
            final Text result = new Text(parent, SINGLE | BORDER);
            result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            return result;
        }
    }
}
