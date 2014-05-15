package pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern;

import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.DOT;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.IDENTIFIER;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.NEWLINE;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.UNKNOWN;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.WHITESPACE;

import java.util.List;

import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token;
import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type;

import com.google.common.base.Optional;

/**
 * Pattern for situation when event of command can be suggested.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class CommandEventPattern extends PatternBase {

	public static enum FunctionType {
		COMMAND, EVENT, ANY;
	};

	private FunctionType type;
	private Optional<String> interfaceName;
	private Optional<String> functionName;

	@Override
	public boolean match(List<Token> tokens, int currentOffset) {
		resetData();

		/*
		 * On empty token list try to suggest anything.
		 */
		if (tokens.isEmpty()) {
			setData(FunctionType.ANY, Optional.<String> absent(), Optional.<String> absent(), currentOffset);
			return true;
		}

		final int size = tokens.size();
		final Token lastToken = tokens.get(size - 1);
		final Type lastTokenType = lastToken.getType();
		final Token secondLastToken = size >= 2 ? tokens.get(size - 2) : null;
		final Type secondTokenType = secondLastToken != null ? secondLastToken.getType() : null;
		final Token thirdLastToken = size >= 3 ? tokens.get(size - 3) : null;
		final Type thirdTokenType = thirdLastToken != null ? thirdLastToken.getType() : null;
		final Token fourthLastToken = size >= 4 ? tokens.get(size - 4) : null;
		final Type fourthTokenType = fourthLastToken != null ? fourthLastToken.getType() : null;
		final Token fifthLastToken = size >= 5 ? tokens.get(size - 5) : null;
		final Type fifthTokenType = fifthLastToken != null ? fifthLastToken.getType() : null;

		/* Try to recognize "call/signal <iface>.<function_prefix>" */
		if (size >= 5 && lastTokenType == IDENTIFIER && secondTokenType == DOT && thirdTokenType == IDENTIFIER
				&& fourthTokenType == WHITESPACE && fifthTokenType == IDENTIFIER) {
			final FunctionType functionType = getFunctionType(fifthLastToken);
			if (functionType != null) {
				setData(functionType, Optional.of(thirdLastToken.getValue()), Optional.of(lastToken.getValue()),
						currentOffset, fifthLastToken, fourthLastToken, thirdLastToken, secondLastToken, lastToken);
				return true;
			}
		}

		/* Try to recognize "call/signal <iface>." */
		if (size >= 4 && lastTokenType == DOT && secondTokenType == IDENTIFIER && thirdTokenType == WHITESPACE
				&& fourthTokenType == IDENTIFIER) {
			final FunctionType functionType = getFunctionType(fourthLastToken);
			if (functionType != null) {
				setData(functionType, Optional.of(secondLastToken.getValue()), Optional.<String> absent(),
						currentOffset, fourthLastToken, thirdLastToken, secondLastToken, lastToken);
				return true;
			}
		}

		/* Try to recognize "call/signal <iface_prefix>" */
		if (size >= 3 && lastTokenType == IDENTIFIER && secondTokenType == WHITESPACE && thirdTokenType == IDENTIFIER) {
			final FunctionType functionType = getFunctionType(thirdLastToken);
			if (functionType != null) {
				setData(functionType, Optional.of(lastToken.getValue()), Optional.<String> absent(), currentOffset,
						thirdLastToken, secondLastToken, lastToken);
				return true;
			}
		}

		/* Try to recognize "call/signal <whitespace>" */
		if (size >= 2 && lastTokenType == WHITESPACE && secondTokenType == IDENTIFIER) {
			final FunctionType functionType = getFunctionType(secondLastToken);
			if (functionType != null) {
				setData(functionType, Optional.<String> absent(), Optional.<String> absent(), currentOffset,
						secondLastToken, lastToken);
				return true;
			}
		}

		/* Try to recognize (prefix of) "call" or "signal" */
		if (lastTokenType == UNKNOWN || lastTokenType == NEWLINE || lastTokenType == WHITESPACE) {
			setData(FunctionType.ANY, Optional.<String> absent(), Optional.<String> absent(), currentOffset);
			return true;
		}
		/* call or signal? */
		else if (lastTokenType == IDENTIFIER) {
			final FunctionType functionType = getFunctionType(lastToken);
			if (functionType != null) {
				setData(functionType, Optional.<String> absent(), Optional.<String> absent(), currentOffset, lastToken);
				return true;
			}
			return false;
		}

		return false;
	}

	@Override
	public <R, A> R accept(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	public FunctionType getType() {
		return type;
	}

	public Optional<String> getInterfaceName() {
		return interfaceName;
	}

	public Optional<String> getFunctionName() {
		return functionName;
	}

	private FunctionType getFunctionType(Token token) {
		final String value = token.getValue();
		if ("call".startsWith(value)) {
			return FunctionType.COMMAND;
		} else if ("signal".startsWith(value)) {
			return FunctionType.EVENT;
		}
		return null;
	}

	private void setData(FunctionType type, Optional<String> ifaceName, Optional<String> functionName,
			int currentOffset, Token... tokens) {
		this.type = type;
		this.interfaceName = ifaceName;
		this.functionName = functionName;

		final StringBuilder builder = new StringBuilder();
		final int offset = tokens.length == 0 ? currentOffset : tokens[0].getOffset();

		for (Token token : tokens) {
			builder.append(token.getValue());
		}

		final String replacementString = builder.toString();
		setData(replacementString, offset, replacementString.length());
	}

	@Override
	public String toString() {
		return "{ CommandEventPattern; {type=" + type + ", interfaceName=" + interfaceName + ", functionName="
				+ functionName + "}}";
	}

}
