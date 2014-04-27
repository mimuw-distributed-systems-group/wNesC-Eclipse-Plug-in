package pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner;

/**
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class Token {

	public static enum Type {
		IDENTIFIER, WHITESPACE,
	};

	private final String value;
	private final Type type;
	private final int offset;
	private final int length;

	public Token(String value, Type type, int offset, int length) {
		this.value = value;
		this.type = type;
		this.offset = offset;
		this.length = length;
	}

	public String getValue() {
		return value;
	}

	public Type getType() {
		return type;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		return "{ Token; {value=" + value + ", type=" + type + ", offset=" + offset + ", length=" + length + "}}";
	}

}
