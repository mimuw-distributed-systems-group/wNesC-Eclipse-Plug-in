package pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern;

import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token;

/**
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 */
public final class PatternUtils {

	/**
	 * Compares two tokens equality on type and value.
	 *
	 * @param first
	 *            first token
	 * @param second
	 *            second token
	 * @return <code>true</code> if tokens has the same type and value
	 */
	public static boolean hasEqualTypeAndValue(Token first, Token second) {
		return (first.getValue().equals(second.getValue()) && first.getType().equals(second.getType()));
	}

}
