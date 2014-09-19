package pl.edu.mimuw.nesc.plugin.wizards.fields;

import org.eclipse.core.runtime.Path;

/**
 * Field that allows entering a name of a file.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class FileNameField extends TextField {
    /**
     * @return Newly created builder for this class.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Initializes this field and creates all of its controls.
     *
     * @param builder Builder for this class.
     */
    private FileNameField(Builder builder) {
        super(builder);
    }

    @Override
    public String getErrorStatus() {
        final String name = getName();
        final String value = getValue();

        if (value.isEmpty()) {
            return name + " is empty.";
        }

        // Check if the entered value is a valid file name
        if (!new Path(value).isValidSegment(value)) {
            return "Entered " + name.toLowerCase() + " is ill-formed.";
        }

        return null;
    }

    /**
     * Builder for this part of the fields hierarchy.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public static final class Builder extends TextField.Builder<FileNameField> {
        /**
         * Data for the creation of the field.
         */
        private String fieldName;

        /**
         * Constructor only for the class of the object that will be built.
         */
        private Builder() {
        }

        /**
         * Set the name of the field.
         *
         * @param fieldName Name that will be associated with the created field.
         * @return <code>this</code>
         */
        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        @Override
        protected void beforeBuild() {
            super.beforeBuild();
            setFieldName(fieldName);
        }

        @Override
        protected FileNameField create() {
            return new FileNameField(this);
        }
    }
}
