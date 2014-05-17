package pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern;

import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token;

/**
 * Pattern base class.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public abstract class PatternBase implements Pattern {

	protected String replacementString;
	protected int length;
	protected int offset;

	protected PatternBase() {
		resetData();
	}

	@Override
	public String getReplacementString() {
		return replacementString;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	protected void resetData() {
		this.replacementString = null;
		this.length = -1;
		this.offset = -1;
	}

	protected void setData(String replacementString, int offset, int length) {
		this.replacementString = replacementString;
		this.length = length;
		this.offset = offset;
	}

	protected void setData(int currentOffset, Token... tokens) {
		final StringBuilder builder = new StringBuilder();
		final int offset = tokens.length == 0 ? currentOffset : tokens[0].getOffset();

		for (Token token : tokens) {
			builder.append(token.getValue());
		}

		final String replacementString = builder.toString();
		setData(replacementString, offset, replacementString.length());
	}
}
