package pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern;

import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.IDENTIFIER;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.NEWLINE;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.UNKNOWN;
import static pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token.Type.WHITESPACE;

import java.util.List;

import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token;

import com.google.common.base.Optional;

/**
 * Pattern for situation when task can be suggested.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class TaskPattern extends PatternBase {

	private static final String POST = "post";

	private Optional<String> name;

	@Override
	public boolean match(List<Token> tokens, int currentOffset) {
		resetData();

		/*
		 * On empty token list try to suggest anything.
		 */
		if (tokens.isEmpty()) {
			setData(Optional.<String> absent(), currentOffset);
			return true;
		}

		final int size = tokens.size();
		final Token lastToken = tokens.get(size - 1);
		final Token penultimateToken = size >= 2 ? tokens.get(size - 2) : null;
		final Token thirdLastToken = size >= 3 ? tokens.get(size - 3) : null;

		/* Try to recognize "post <function_prefix>" */
		if (size >= 3 && lastToken.getType() == IDENTIFIER && penultimateToken.getType() == WHITESPACE
				&& thirdLastToken.getType() == IDENTIFIER && POST.equals(thirdLastToken.getValue())) {
			setData(Optional.of(lastToken.getValue()), currentOffset, thirdLastToken, penultimateToken, lastToken);
			return true;
		}

		/* Try to recognize "post <whitespaces>" */
		if (size >= 2 && lastToken.getType() == WHITESPACE && penultimateToken.getType() == IDENTIFIER
				&& POST.equals(penultimateToken.getValue())) {
			setData(Optional.<String> absent(), currentOffset, penultimateToken, lastToken);
			return true;
		}

		/* Try to recognize "<post_prefix>" */
		if (lastToken.getType() == IDENTIFIER && POST.startsWith(lastToken.getValue())) {
			setData(Optional.<String> absent(), currentOffset, lastToken);
			return true;
		}

		if (lastToken.getType() == UNKNOWN || lastToken.getType() == WHITESPACE || lastToken.getType() == NEWLINE) {
			setData(Optional.<String> absent(), currentOffset);
			return true;
		}
		return false;
	}

	@Override
	public <R, A> R accept(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	public Optional<String> getName() {
		return name;
	}

	private void setData(Optional<String> taskName, int currentOffset, Token... tokens) {
		this.name = taskName;
		setData(currentOffset, tokens);
	}

}
