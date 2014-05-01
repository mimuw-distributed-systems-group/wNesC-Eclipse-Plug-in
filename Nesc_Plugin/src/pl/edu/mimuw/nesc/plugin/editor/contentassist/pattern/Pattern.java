package pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern;

import java.util.List;

import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token;

/**
 * <p>
 * Represents a language construct pattern.
 * </p>
 * <p>
 * Recognized token in source file are tested if matches given pattern. Then
 * each matched pattern is the input for content proposal computation. When
 * pattern is being verified the semantic data is also collected and then used
 * during proposals computation.
 * </p>
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public interface Pattern {

	/**
	 * Determines if given <code>tokens</code> list at given <code>offset</code>
	 * matches to the pattern.
	 *
	 * @param tokens
	 *            tokens list
	 * @param currentOffset
	 *            current offset
	 * @return <code>true</code> if pattern was recognized
	 */
	boolean match(List<Token> tokens, int currentOffset);

	/**
	 * Gets the replacement string.
	 *
	 * @return replacement string
	 */
	String getReplacementString();

	/**
	 * Gets the length of the text to be replaced.
	 *
	 * @return length of the text to be replaced
	 */
	int getLength();

	/**
	 * Gets the offset of the text to be replaced.
	 *
	 * @return offset of the text to be replaced
	 */
	int getOffset();

	<R, A> R accept(Visitor<R, A> visitor, A arg);

	public interface Visitor<R, A> {
		R visit(VariablePattern pattern, A arg);
	}

}
