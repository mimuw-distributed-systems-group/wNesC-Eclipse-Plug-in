package pl.edu.mimuw.nesc.plugin.preferences.util;

import com.google.common.base.Preconditions;
import java.util.regex.Pattern;

/**
 * Class whose objects are responsible of validating NesC comments. It follows
 * the Singleton design pattern.
 *
 * @author Michał Ciszewski <michal.ciszewski@students.mimuw.edu.pl>
 */
public final class NescCommentValidator {
    /**
     * Regular expressions related to line terminators.
     */
    private static final String REGEXP_LINE_TERMINATOR = "((\\n)|(\\r\\n)|(\\r)|(\\u0085)|(\\u2028)|(\\u2029))";
    private static final String REGEXP_NOT_LINE_TERMINATOR = "[^\\n\\r\\u0085\\u2028\\u2029]";

    /**
     * Regular expressions whose languages are those of valid nesC comments.
     */
    private static final String REGEXP_NESC_MULTILINE_COMMENT = "(/\\*.*\\*/)";
    private static final String REGEXP_NESC_SINGLELINE_COMMENT = "(//" + REGEXP_NOT_LINE_TERMINATOR + "*"
            + REGEXP_LINE_TERMINATOR + "?)+";
    private static final String REGEXP_NESC_COMMENT = "\\A(" + REGEXP_NESC_SINGLELINE_COMMENT + "|"
            + REGEXP_NESC_MULTILINE_COMMENT + ")?\\z";

    /**
     * The only instance of this class.
     */
    private static final NescCommentValidator instance = new NescCommentValidator();

    /**
     * Pattern for the matching operations.
     */
    private final Pattern nescCommentPattern = Pattern.compile(REGEXP_NESC_COMMENT, Pattern.DOTALL);

    /**
     * Private for the Singleton design pattern.
     */
    private NescCommentValidator() {}

    /**
     * @return The only instance of this class.
     */
    public static NescCommentValidator getInstance() {
        return instance;
    }

    /**
     * Validates the given NesC comment.
     *
     * @param comment Comment to be validated.
     * @return True if and only if the given comment is a valid NesC comment
     *         or sequence of valid NesC comments of a, here, indefinite type.
     *         The empty string is also accepted.
     * @throws NullPointerException Given argument is null.
     */
    public boolean validate(CharSequence comment) {
        Preconditions.checkNotNull(comment, "comment cannot be null");
        return nescCommentPattern.matcher(comment).matches();
    }
}
