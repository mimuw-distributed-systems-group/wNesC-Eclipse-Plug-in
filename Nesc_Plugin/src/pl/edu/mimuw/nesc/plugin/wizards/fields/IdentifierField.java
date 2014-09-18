package pl.edu.mimuw.nesc.plugin.wizards.fields;

/**
 * Class that represents a wizard field that allows entering an identifier,
 * e.g. a nesC interface, module or component name.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class IdentifierField extends TextField {

    /**
     * Get the builder for this class.
     *
     * @return Newly created builder that will build an object of this class.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Initializes this object.
     *
     * @param builder A builder object for this class.
     */
    private IdentifierField(Builder builder) {
        super(builder);
    }

    @Override
    public String getErrorStatus() {
        final String fieldName = getName();
        final String value = getValue();

        switch(IdentifierValidator.getSyntaxInstance().validate(value)) {
        case SUCCESS:
            return null;
        case EMPTY:
            return fieldName + " cannot be empty.";
        case FIRST_CHAR_DIGIT:
            return "The first character of the " + fieldName.toLowerCase() + " cannot be a digit.";
        case FORBIDDEN_CHAR:
            return "Only letters, digits and underscores are allowed in the "
                + fieldName.toLowerCase() + ".";
        default:
            throw new RuntimeException("Unsupported identifier validation result.");
        }
    }

    /**
     * Builder for this part of the fields hierarchy.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public static final class Builder extends TextField.Builder<IdentifierField> {
        /**
         * Fields for this object.
         */
        private String fieldName;

        /**
         * Constructor only for the class of the object being built.
         */
        private Builder() {
        }

        /**
         * Set the name of the field that will be created.
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
        protected IdentifierField create() {
            return new IdentifierField(this);
        }
    }
}
