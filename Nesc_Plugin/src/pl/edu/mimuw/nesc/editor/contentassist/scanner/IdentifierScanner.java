package pl.edu.mimuw.nesc.editor.contentassist.scanner;

import static pl.edu.mimuw.nesc.editor.contentassist.scanner.Token.Type.IDENTIFIER;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Scanner that only tries to recognize last identifier.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class IdentifierScanner implements ContextScanner {

	@Override
	public List<Token> getTokens(IDocument doc, int offset) {
		try {
			for (int n = offset - 1; n >= 0; --n) {
				char c = doc.getChar(n);
				if (!Character.isJavaIdentifierPart(c)) {
					final int off = n + 1;
					final int length = offset - n - 1;
					final String value = doc.get(off, offset - n - 1);
					final Token token = new Token(value, IDENTIFIER, off, length);
					return Collections.singletonList(token);
				}
			}
		} catch (BadLocationException e) {
			// TODO: log
		}
		return new LinkedList<>();

	}
}
