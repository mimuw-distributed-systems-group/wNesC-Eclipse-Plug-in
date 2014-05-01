package pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern;

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
}
