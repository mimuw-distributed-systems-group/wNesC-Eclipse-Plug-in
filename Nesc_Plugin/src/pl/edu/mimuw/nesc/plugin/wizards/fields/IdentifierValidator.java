package pl.edu.mimuw.nesc.plugin.wizards.fields;

/**
 * Class whose objects are responsible of checking the correctness of
 * nesC identifiers.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */

public class IdentifierValidator {
    /**
     * Empty array constant used for the default construction.
     */
    private static final String[] EMPTY_ARRAY = {};

    /**
     * Instance that checks only syntax of identifiers.
     */
    private static final IdentifierValidator SYNTAX_INSTANCE = new IdentifierValidator();

    /**
     * Regular expression that defines the language of the valid values of
     * identifiers.
     */
    private static final String IDENTIFIER_REGEXP = "^[A-Za-z_]\\w*$";

    /**
     * Array of identifiers that must differ from the validated one if the
     * validation is to succeed. Never null.
     */
    private final String[] forbiddenIdentifiers;

    /**
     * @return <code>IdentifierValidator</code> instance that will check only
     *         the syntax of the identifier (with empty list of forbidden
     *         identifiers).
     */
    public static IdentifierValidator getSyntaxInstance() {
        return SYNTAX_INSTANCE;
    }

    /**
     * Initializes the validator with a null array of forbidden identifiers.
     */
    private IdentifierValidator() {
        this(EMPTY_ARRAY);
    }

    /**
     * Initializes the validator with given forbidden identifiers.
     *
     * @param forbiddenIdentifiers Array of identifiers that must differ for the
     *                             validated one if the validation is to
     *                             succeed.
     * @throws NullPointerException The argument is null.
     * @throws IllegalArgumentException One of the array elements is null.
     */
    public IdentifierValidator(String[] forbiddenIdentifiers) {
        // Check the correctness of the argument
        if (forbiddenIdentifiers == null) {
            throw new NullPointerException("Forbidden identifiers array is null.");
        }
        for (String identifier : forbiddenIdentifiers) {
            if (identifier == null) {
                throw new IllegalArgumentException("Forbidden identifier is null.");
            }
        }

        this.forbiddenIdentifiers = forbiddenIdentifiers;
    }

    /**
     * Validates the given identifier.
     *
     * @param uncertainIdentifier Identifier to be validated.
     * @return Result of the validation.
     * @throws NullPointerException The argument is null.
     */
    public Result validate(String uncertainIdentifier) {
        // Check the correctness of the call
        if (uncertainIdentifier == null) {
            throw new NullPointerException("Null identifier to validate.");
        }

        // Validate the identifier
        if (!uncertainIdentifier.matches(IDENTIFIER_REGEXP)) {
            if ("".equals(uncertainIdentifier)) {
                return Result.EMPTY;
            } else if (Character.isDigit(uncertainIdentifier.charAt(0))) {
                return Result.FIRST_CHAR_DIGIT;
            } else {
                return Result.FORBIDDEN_CHAR;
            }
        }

        // Check if the identifier is unique
        for (String forbiddenIdentifier : forbiddenIdentifiers) {
            if (forbiddenIdentifier.equals(uncertainIdentifier)) {
                return Result.DUPLICATE;
            }
        }

        return Result.SUCCESS;
    }

    /**
     * Enumeration type that represents the result of the validation.
     *
     * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
     */
    public enum Result {
        SUCCESS,
        EMPTY,
        FORBIDDEN_CHAR,
        FIRST_CHAR_DIGIT,
        DUPLICATE
    }
}
