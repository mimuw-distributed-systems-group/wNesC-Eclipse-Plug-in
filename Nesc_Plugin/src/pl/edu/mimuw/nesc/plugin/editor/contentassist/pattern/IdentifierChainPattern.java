package pl.edu.mimuw.nesc.plugin.editor.contentassist.pattern;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import pl.edu.mimuw.nesc.plugin.editor.contentassist.scanner.Token;

import com.google.common.base.Optional;

/**
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class IdentifierChainPattern extends PatternBase {

	private List<Token> tokensList;
	private Optional<Token> lastIdToken;
	private List<String> identifierList;
	private EnumSet<Token.Type> separatorTypes;
	private int separatorCount;

	@Override
	public boolean match(List<Token> tokens, int currentOffset) {
		resetData();

		Optional<Token> lastIdToken;
		final LinkedList<Token> resultList = new LinkedList<>();

		final LinkedList<Token> filteredTokens = new LinkedList<>();
		for (int i = tokens.size() - 1; i >= 0; --i) {
			final Token token = tokens.get(i);
			final Token.Type type = token.getType();
			if (type == Token.Type.DOT || type == Token.Type.RIGHT_ARROW || type == Token.Type.IDENTIFIER) {
				filteredTokens.addLast(token);
			} else if (type == Token.Type.WHITESPACE) {
				continue;
			} else {
				break;
			}
		}
		/* filteredTokens list contains tokens in reversed order. */

		final int size = filteredTokens.size();
		/* At least one separator and one identifier is expected. */
		if (size < 2) {
			return false;
		}

		boolean expectedSeparator;
		final Token firstToken = filteredTokens.removeFirst();
		if (firstToken.getType() == Token.Type.IDENTIFIER) {
			lastIdToken = Optional.of(firstToken);
			expectedSeparator = true;
		} else {
			lastIdToken = Optional.<Token> absent();
			expectedSeparator = false;
		}
		resultList.add(firstToken);

		/*
		 * Try to find the longest interlaced chain of separators and
		 * identifiers.
		 */
		for (Token token : filteredTokens) {
			final Token.Type type = token.getType();
			if (expectedSeparator && (type == Token.Type.IDENTIFIER)) {
				break;
			}
			resultList.addFirst(token);
			expectedSeparator = !expectedSeparator;
		}

		/* Again, at least one separator and one identifier is expected. */
		if (size < 2) {
			return false;
		}

		setData(currentOffset, resultList, lastIdToken);
		return true;
	}

	@Override
	public <R, A> R accept(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	public List<Token> getTokensList() {
		return tokensList;
	}

	public Optional<Token> getLastIdToken() {
		return lastIdToken;
	}

	public List<String> getIdentifierList() {
		return identifierList;
	}

	public EnumSet<Token.Type> getSeparatorTypes() {
		return separatorTypes;
	}

	public int getSeparatorCount() {
		return separatorCount;
	}

	@Override
	protected void resetData() {
		super.resetData();
		this.tokensList = null;
		this.lastIdToken = null;
		this.identifierList = null;
		this.separatorTypes = null;
		this.separatorCount = 0;
	}

	private void setData(int currentOffset, List<Token> tokensList, Optional<Token> lastIdToken) {
		this.tokensList = tokensList;
		this.lastIdToken = lastIdToken;
		this.identifierList = new ArrayList<>();
		this.separatorTypes = EnumSet.noneOf(Token.Type.class);
		this.separatorCount = 0;

		for (Token token : tokensList) {
			Token.Type type = token.getType();
			if (type == Token.Type.IDENTIFIER) {
				this.identifierList.add(token.getValue());
			}
			if (type == Token.Type.DOT || type == Token.Type.RIGHT_ARROW) {
				this.separatorTypes.add(type);
				this.separatorCount++;
			}
		}
		if (!lastIdToken.isPresent()) {
			this.identifierList.add("");
		}

		if (lastIdToken.isPresent()) {
			super.setData(currentOffset, lastIdToken.get());
		} else {
			super.setData(currentOffset);
		}
	}
}
