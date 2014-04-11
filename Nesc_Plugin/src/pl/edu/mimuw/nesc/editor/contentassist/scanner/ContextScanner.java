package pl.edu.mimuw.nesc.editor.contentassist.scanner;

import java.util.List;

import org.eclipse.jface.text.IDocument;

/**
 * Scanner determining the text context for content assistance. Reads backwards
 * characters preceding cursor and tries to recognize tokens using implemented
 * strategy.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public interface ContextScanner {

	/**
	 * Returns list of recognized tokens.
	 *
	 * @param doc
	 *            document
	 * @param offset
	 *            cursor's offset in document
	 * @return list of tokens
	 */
	List<Token> getTokens(IDocument doc, int offset);

}
