package pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern;

import static pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern.PatternUtils.hasEqualTypeAndValue;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.DOT;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.IDENTIFIER;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.NEWLINE;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.RIGHT_ARROW;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.UNKNOWN;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.WHITESPACE;

import java.util.List;

import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type;

import com.google.common.collect.Lists;

/**
 * Pattern for situation when variable, typedef name or enum constant can be
 * suggested.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class VariablePattern extends PatternBase {

	private static final List<Token> FORBIDDEN_PRECEDING_TOKENS = Lists.newArrayList(
			new Token(".", DOT),
			new Token("->", RIGHT_ARROW),
			new Token("call", IDENTIFIER),
			new Token("signal", IDENTIFIER));

	private String name;

	@Override
	public boolean match(List<Token> tokens, int currentOffset) {
		resetData();

		/*
		 * On empty token list try to suggest anything.
		 */
		if (tokens.isEmpty()) {
			setData(new Token("", IDENTIFIER, currentOffset, 0));
			return true;
		}

		final int size = tokens.size();
		final Token lastToken = tokens.get(size - 1);
		final Token penultimateToken = size >= 2 ? tokens.get(size - 2) : null;
		final Token thirdLastToken = size >= 3 ? tokens.get(size - 3) : null;

		final Type lastTokenType = lastToken.getType();
		if (lastTokenType == UNKNOWN || lastTokenType == NEWLINE) {
			setData(new Token("", IDENTIFIER, currentOffset, 0));
			return true;
		}
		/*
		 * The last token is whitespace.
		 */
		else if (lastToken.getType() == Type.WHITESPACE) {
			if (penultimateToken != null) {
				if (isForbidden(penultimateToken)) {
					return false;
				}
			}
			setData(new Token("", IDENTIFIER, currentOffset, 0));
			return true;
		}
		/*
		 * The last token is an identifier prefix.
		 */
		else if (lastToken.getType() == IDENTIFIER) {
			/* Ignore whitespaces preceding identifier. */
			final Token precedingToken;
			if (penultimateToken != null && penultimateToken.getType() == WHITESPACE) {
				precedingToken = thirdLastToken;
			} else {
				precedingToken = penultimateToken;
			}
			if (precedingToken != null && isForbidden(precedingToken)) {
				return false;
			}
			setData(lastToken);
			return true;
		}
		return false;
	}

	@Override
	public <R, A> R accept(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	public String getName() {
		return name;
	}

	private boolean isForbidden(Token token) {
		for (Token forbidden : FORBIDDEN_PRECEDING_TOKENS) {
			if (hasEqualTypeAndValue(token, forbidden)) {
				return true;
			}
		}
		return false;
	}

	private void setData(Token token) {
		this.name = token.getValue();
		setData(this.name, token.getOffset(), token.getLength());
	}

}
