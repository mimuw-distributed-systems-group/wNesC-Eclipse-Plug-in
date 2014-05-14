package pl.edu.mimuw.nesc.plugin.wizards.fields;

/**
 * An interface for a wizard field (which consists of a label and controls that
 * allow to set a value for the field).
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public interface WizardField {
    /**
     * Sets focus to the field control that allows entering the field value.
     */
    void setFocus();

    /**
     * Lets the user enter a value to the field or not.
     *
     * @param enabled Specifies whether the field will be enabled or disabled.
     */
    void setEnabled(boolean enabled);

    /**
     * @return String describing why the value of the field is invalid. If it is
     *         valid, null should be returned.
     */
    String getErrorStatus();

    /**
     * @return Object that represents a value entered in the field. For
     *         a concrete subclass it should be an object of a concrete type
     *         - always the same.
     */
    Object getValue();

    /**
     * @return Name of the value that the user will be allowed to enter in this
     *         field. It can be e.g. the text of the value for the field.
     */
    String getName();
}
