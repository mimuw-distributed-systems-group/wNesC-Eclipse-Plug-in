package pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import com.google.common.collect.Lists;

/**
 * Scanner that tries to recognize identifiers, whitespace blocks, arrows and
 * dots until newline or other character is met.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class DefaultPartitionScanner implements ContextScanner {

	private enum State {
		INITIAL, TERMINAL, IDENTIFIER, WHITESPACE,
	};

	private static final List<Character> NEWLINE_CHARACTER = Lists.newArrayList('\r', '\n');

	private IDocument doc;
	private int currentPos;
	private int lastPos;
	private List<Token> result;
	private State state;

	private void reset(IDocument doc, int offset) {
		this.doc = doc;
		this.currentPos = offset;
		this.lastPos = offset;
		this.result = new ArrayList<>();
		this.state = State.INITIAL;
	}

	@Override
	public List<Token> getTokens(IDocument doc, int offset) {
		reset(doc, offset);
		try {
			scan();
		} catch (BadLocationException e) {
			// TODO: log
		}
		Collections.reverse(result);
		return result;
	}

	private void scan() throws BadLocationException {
		while (true) {
			switch (state) {
			case INITIAL:
				initial();
				break;
			case IDENTIFIER:
				identifier();
				break;
			case WHITESPACE:
				whitespace();
				break;
			case TERMINAL:
				/* finish scanning. */
				return;
			default:
				throw new IllegalStateException("Unknown state: " + state);
			}
		}
	}

	private void initial() throws BadLocationException {
		final char c = nextChar();
		/* identifier */
		if (Character.isJavaIdentifierPart(c)) {
			this.state = State.IDENTIFIER;
		}
		/* newline - finish scanning */
		else if (NEWLINE_CHARACTER.contains(c)) {
			result.add(new Token(getValue(), Token.Type.NEWLINE, currentPos, getLength()));
			this.state = State.TERMINAL;
		}
		/* whitespace (excluding newline) */
		else if (Character.isWhitespace(c)) {
			this.state = State.WHITESPACE;
		}
		/* left arrow */
		else if (c == '-') {
			final char d = nextChar();
			if (d == '<') {
				result.add(new Token(getValue(), Token.Type.LEFT_ARROW, currentPos, getLength()));
				this.state = State.INITIAL;
			} else {
				currentPos++;
				result.add(new Token(getValue(), Token.Type.UNKNOWN, currentPos, getLength()));
				this.state = State.TERMINAL;
			}
			lastPos = currentPos;
		}
		/* right arrow */
		else if (c == '>') {
			final char d = nextChar();
			if (d == '-') {
				result.add(new Token(getValue(), Token.Type.RIGHT_ARROW, currentPos, getLength()));
				this.state = State.INITIAL;
			} else {
				currentPos++;
				result.add(new Token(getValue(), Token.Type.UNKNOWN, currentPos, getLength()));
				this.state = State.TERMINAL;
			}
			lastPos = currentPos;
		}
		/* dot */
		else if (c == '.') {
			result.add(new Token(getValue(), Token.Type.DOT, currentPos, getLength()));
			this.state = State.INITIAL;
			lastPos = currentPos;
		}
		/* unrecognized */
		else {
			result.add(new Token(getValue(), Token.Type.UNKNOWN, currentPos, getLength()));
			this.state = State.TERMINAL;
		}
	}

	private void identifier() throws BadLocationException {
		final char c = nextChar();
		if (!Character.isJavaIdentifierPart(c)) {
			currentPos++;
			result.add(new Token(getValue(), Token.Type.IDENTIFIER, currentPos, getLength()));

			lastPos = currentPos;
			this.state = State.INITIAL;
		}
	}

	private void whitespace() throws BadLocationException {
		final char c = nextChar();
		if (!Character.isWhitespace(c) || NEWLINE_CHARACTER.contains(c)) {
			currentPos++;
			result.add(new Token(getValue(), Token.Type.WHITESPACE, currentPos, getLength()));

			lastPos = currentPos;
			this.state = State.INITIAL;
		}
	}

	private String getValue() throws BadLocationException {
		return doc.get(currentPos, getLength());
	}

	private int getLength() {
		return lastPos - currentPos;
	}

	private char nextChar() throws BadLocationException {
		return doc.getChar(--currentPos);
	}
}
