package pl.edu.mimuw.nesc.plugin.editor.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.FastPartitioner;

import pl.edu.mimuw.nesc.plugin.partitioning.FastNescPartitionScanner;
import pl.edu.mimuw.nesc.plugin.partitioning.INCPartitions;

public class NescAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {
	
	private String fPartitioning;
	private static final String LINE_COMMENT= "//";

	public NescAutoIndentStrategy(String partitioning) {
		this.fPartitioning = partitioning;
	}

	/*
	 * (non-Javadoc) Method declared on IAutoIndentStrategy
	 */
	@Override
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		boolean isNewline = c.length == 0 && c.text != null && endsWithDelimiter(d, c.text);
		if (isNewline) {
			smartIndentAfterNewLine(d, c);
		} else if (c.text.length() == 1) {
			if (c.text.charAt(0) == '}') {
				smartInsertAfterClosingBracket(d, c);
			}
		} else if (c.text.length() > 1 && c.text.trim().length() > 0) {
			smartPaste(d, c);
		}			
	}

	/**
	 * Returns whether or not the given text ends with one of the documents
	 * legal line delimiters.
	 * 
	 * @param d
	 *            the document
	 * @param txt
	 *            the text
	 * @return <code>true</code> if <code>txt</code> ends with one of the
	 *         document's line delimiters, <code>false</code> otherwise
	 */
	private boolean endsWithDelimiter(IDocument d, String txt) {
		String[] delimiters = d.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.endsWith(delimiters, txt) > -1;
		return false;
	}

	/**
	 * Returns the line number of the next bracket after end.
	 * 
	 * @param document
	 *            - the document being parsed
	 * @param line
	 *            - the line to start searching back from
	 * @param end
	 *            - the end position to search back from
	 * @param closingBracketIncrease
	 *            - the number of brackets to skip
	 * @return the line number of the next matching bracket after end
	 * @throws BadLocationException
	 *             in case the line numbers are invalid in the document
	 */
	protected int findMatchingOpenBracket(IDocument document, int line,
			int end, int closingBracketIncrease) throws BadLocationException {

		int start = document.getLineOffset(line);
		int brackcount = getBracketCount(document, start, end, false)
				- closingBracketIncrease;

		// sum up the brackets counts of each line (closing brackets count
		// negative,
		// opening positive) until we find a line the brings the count to zero
		while (brackcount < 0) {
			line--;
			if (line < 0) {
				return -1;
			}
			start = document.getLineOffset(line);
			end = start + document.getLineLength(line) - 1;
			brackcount += getBracketCount(document, start, end, false);
		}
		return line;
	}

	/**
	 * Returns the bracket value of a section of text. Closing brackets have a
	 * value of -1 and open brackets have a value of 1.
	 * 
	 * @param document
	 *            - the document being parsed
	 * @param start
	 *            - the start position for the search
	 * @param end
	 *            - the end position for the search
	 * @param ignoreCloseBrackets
	 *            - whether or not to ignore closing brackets in the count
	 * @return the bracket value of a section of text
	 * @throws BadLocationException
	 *             in case the positions are invalid in the document
	 */
	private int getBracketCount(IDocument document, int start, int end, boolean ignoreCloseBrackets) throws BadLocationException {
		int bracketcount = 0;
		while (start < end) {
			char curr = document.getChar(start);
			start++;
			switch (curr) {
			case '/':
				if (start < end) {
					char next = document.getChar(start);
					if (next == '*') {
						// a comment starts, advance to the comment end
						start = getCommentEnd(document, start + 1, end);
					} else if (next == '/') {
						// '//'-comment: nothing to do anymore on this line
						start = end;
					}
				}
				break;
			case '*':
				if (start < end) {
					char next = document.getChar(start);
					if (next == '/') {
						// we have been in a comment: forget what we read before
						bracketcount = 0;
						start++;
					}
				}
				break;
			case '{':
				bracketcount++;
				ignoreCloseBrackets = false;
				break;
			case '}':
				if (!ignoreCloseBrackets) {
					bracketcount--;
				}
				break;
			case '"':
			case '\'':
				start = getStringEnd(document, start, end, curr);
				break;
			default:
			}
		}
		return bracketcount;
	}

	/**
	 * Returns the end position of a comment starting at the given
	 * <code>position</code>.
	 * 
	 * @param document
	 *            - the document being parsed
	 * @param position
	 *            - the start position for the search
	 * @param end
	 *            - the end position for the search
	 * @return the end position of a comment starting at the given
	 *         <code>position</code>
	 * @throws BadLocationException
	 *             in case <code>position</code> and <code>end</code> are
	 *             invalid in the document
	 */
	private int getCommentEnd(IDocument document, int position, int end)
			throws BadLocationException {
		int currentPosition = position;
		while (currentPosition < end) {
			char curr = document.getChar(currentPosition);
			currentPosition++;
			if (curr == '*') {
				if (currentPosition < end
						&& document.getChar(currentPosition) == '/') {
					return currentPosition + 1;
				}
			}
		}
		return end;
	}

	/**
	 * Returns the content of the given line without the leading whitespace.
	 * 
	 * @param document
	 *            - the document being parsed
	 * @param line
	 *            - the line being searched
	 * @return the content of the given line without the leading whitespace
	 * @throws BadLocationException
	 *             in case <code>line</code> is invalid in the document
	 */
	protected String getIndentOfLine(IDocument document, int line)
			throws BadLocationException {
		if (line > -1) {
			int start = document.getLineOffset(line);
			int end = start + document.getLineLength(line) - 1;
			int whiteend = findEndOfWhiteSpace(document, start, end);
			return document.get(start, whiteend - start);
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the position of the <code>character</code> in the
	 * <code>document</code> after <code>position</code>.
	 * 
	 * @param document
	 *            - the document being parsed
	 * @param position
	 *            - the position to start searching from
	 * @param end
	 *            - the end of the document
	 * @param character
	 *            - the character you are trying to match
	 * @return the next location of <code>character</code>
	 * @throws BadLocationException
	 *             in case <code>position</code> is invalid in the document
	 */
	private int getStringEnd(IDocument document, int position, int end,
			char character) throws BadLocationException {
		int currentPosition = position;
		while (currentPosition < end) {
			char currentCharacter = document.getChar(currentPosition);
			currentPosition++;
			if (currentCharacter == '\\') {
				// ignore escaped characters
				currentPosition++;
			} else if (currentCharacter == character) {
				return currentPosition;
			}
		}
		return end;
	}

	/**
	 * Set the indent of a new line based on the command provided in the
	 * supplied document.
	 * 
	 * @param document
	 *            - the document being parsed
	 * @param command
	 *            - the command being performed
	 */
	protected void smartIndentAfterNewLine(IDocument document, DocumentCommand command) {

		int docLength = document.getLength();
		if (command.offset == -1 || docLength == 0)
			return;

		// TODO: If we ever introduce a NescIndenter similar to the CIndenter from CDT please consider modyfing this code
		// to better use the patterns found in CDT
		// int addIndent = 0;
		NescHeuristicScanner scanner = new NescHeuristicScanner(document);
		try {
			/*
			ITypedRegion partition= TextUtilities.getPartition(d, fPartitioning, c.offset, false);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType()) && c.offset > 0 && d.getChar(c.offset-1) == '\\') {
				scanner = new CHeuristicScanner(d, fPartitioning, ICPartitions.C_PREPROCESSOR);
				addIndent= 1;
			}
			*/
			int line = document.getLineOfOffset(command.offset);
			IRegion reg = document.getLineInformation(line);
			int start = reg.getOffset();
			int lineEnd = start + reg.getLength();

			// calculating the indent
			StringBuffer indent = new StringBuffer();
			if (command.offset < docLength && document.getChar(command.offset) == '}') {
				int indLine = findMatchingOpenBracket(document, line, command.offset, 0);
				if (indLine == -1) {
					indLine = line;
				}
				indent.append(getIndentOfLine(document, indLine));
			} else {
				int whiteend = findEndOfWhiteSpace(document, start, command.offset);
				indent.append(document.get(start, whiteend - start));
				if (getBracketCount(document, start, command.offset, true) > 0) {
					indent.append("    ");
				}
			}
			
			/*
			StringBuilder indent= null;
			CIndenter indenter= new CIndenter(d, scanner, fProject);
			if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_AUTO_INDENT)) {
				indent= indenter.computeIndentation(c.offset);
			} else {
				// reuse existing indent
				int wsEnd= findEndOfWhiteSpace(d, start, c.offset);
				if (wsEnd > start) {
					indent= new StringBuilder(d.get(start, wsEnd - start));
					addIndent= 0;
				}
			}
			if (indent == null) {
				indent= new StringBuilder();
			}
			if (addIndent > 0 && indent.length() == 0) {
				indent= indenter.createReusingIndent(indent, addIndent, 0);
			}
			*/
			
			StringBuilder buf = new StringBuilder(command.text + indent);

			int contentStart = findEndOfWhiteSpace(document, command.offset, lineEnd);
			command.length =  Math.max(contentStart - command.offset, 0);
			// insert closing brace on new line after an unclosed opening brace
			if (getBracketCount(document, start, command.offset, true) > 0 && !isClosedBrace(document, command.offset, command.length)) {
				command.caretOffset = command.offset + buf.length();
				command.shiftsCaret = false;

				// copy old content of line behind insertion point to new line
				// unless we think we are inserting an anonymous type definition
				if (command.offset == 0 || !(computeAnonymousPosition(document, command.offset - 1, fPartitioning, lineEnd) != -1)) {
					if (lineEnd - contentStart > 0) {
						command.length =  lineEnd - command.offset;
						buf.append(document.get(contentStart, lineEnd - contentStart).toCharArray());
					}
				}

				buf.append(TextUtilities.getDefaultLineDelimiter(document));
				StringBuilder reference = null;
				int nonWS = findEndOfWhiteSpace(document, start, lineEnd);
				//if (nonWS < command.offset && document.getChar(nonWS) == '{')
					reference = new StringBuilder(document.get(start, nonWS - start));
				//else
				//	reference = indenter.getReferenceIndentation(command.offset);
				
				if (reference != null)
					buf.append(reference);
				buf.append('}');
				int bound= command.offset > 200 ? command.offset - 200 : NescHeuristicScanner.UNBOUND;
				int bracePos = scanner.findOpeningPeer(command.offset - 1, bound, '{', '}');
				if (bracePos != NescHeuristicScanner.NOT_FOUND) {
					if (scanner.looksLikeCompositeTypeDefinitionBackward(bracePos, bound) ||
							scanner.previousToken(bracePos - 1, bound) == Symbols.TokenEQUAL) {
						buf.append(';');
					}
				}
			}
			// insert extra line upon new line between two braces
			else if (command.offset > start && contentStart < lineEnd && document.getChar(contentStart) == '}') {
				int firstCharPos = scanner.findNonWhitespaceBackward(command.offset - 1, start);
				if (firstCharPos != NescHeuristicScanner.NOT_FOUND && document.getChar(firstCharPos) == '{') {
					command.caretOffset = command.offset + buf.length();
					command.shiftsCaret = false;

					StringBuilder reference = null;
					int nonWS = findEndOfWhiteSpace(document, start, lineEnd);
					//if (nonWS < command.offset && document.getChar(nonWS) == '{')
						reference = new StringBuilder(document.get(start, nonWS - start));
					//else
					//	reference = indenter.getReferenceIndentation(command.offset);

					buf.append(TextUtilities.getDefaultLineDelimiter(document));

					if (reference != null)
						buf.append(reference);
				}
			}
			command.text = buf.toString();

		} catch (BadLocationException excp) {
			//System.out.println(JavaEditorMessages.getString("AutoIndent.error.bad_location_1")); //$NON-NLS-1$
		}
	}

	/**
	 * Set the indent of a bracket based on the command provided in the supplied
	 * document.
	 * 
	 * @param document
	 *            - the document being parsed
	 * @param command
	 *            - the command being performed
	 */
	protected void smartInsertAfterClosingBracket(IDocument document, DocumentCommand command) {
		if (command.offset == -1 || document.getLength() == 0)
			return;

		try {
			int p = (command.offset == document.getLength() ? command.offset - 1
					: command.offset);
			int line = document.getLineOfOffset(p);
			int start = document.getLineOffset(line);
			int whiteend = findEndOfWhiteSpace(document, start, command.offset);

			// shift only when line does not contain any text up to the closing
			// bracket
			if (whiteend == command.offset) {
				// evaluate the line with the opening bracket that matches out
				// closing bracket
				int indLine = findMatchingOpenBracket(document, line,
						command.offset, 1);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					StringBuffer replaceText = new StringBuffer(
							getIndentOfLine(document, indLine));
					// add the rest of the current line including the just added
					// close bracket
					replaceText.append(document.get(whiteend, command.offset
							- whiteend));
					replaceText.append(command.text);
					// modify document command
					command.length = command.offset - start;
					command.offset = start;
					command.text = replaceText.toString();
				}
			}
		} catch (BadLocationException excp) {
			//System.out.println(JavaEditorMessages.getString("AutoIndent.error.bad_location_2")); //$NON-NLS-1$
		}
	}
	
	private void smartPaste(IDocument document, DocumentCommand command) {
		int newOffset= command.offset;
		int newLength= command.length;
		String newText= command.text;

		try {
			NescHeuristicScanner scanner= new NescHeuristicScanner(document);
			NescIndenter indenter= new NescIndenter(document, scanner);
			int offset= newOffset;

			// reference position to get the indent from
			int refOffset= indenter.findReferencePosition(offset);
			if (refOffset == NescHeuristicScanner.NOT_FOUND)
				return;
			int peerOffset= getPeerPosition(document, command);
			peerOffset= indenter.findReferencePosition(peerOffset);
			if (peerOffset == NescHeuristicScanner.NOT_FOUND)
				return;
			refOffset= Math.min(refOffset, peerOffset);

			// eat any WS before the insertion to the beginning of the line
			int firstLine= 1; // don't format the first line per default, as it has other content before it
			IRegion line= document.getLineInformationOfOffset(offset);
			String notSelected= document.get(line.getOffset(), offset - line.getOffset());
			if (notSelected.trim().length() == 0) {
				newLength += notSelected.length();
				newOffset= line.getOffset();
				firstLine= 0;
			}

			// Prefix: the part we need for formatting but won't paste.
			// Take up to 100 previous lines to preserve enough context.
			int firstPrefixLine= Math.max(document.getLineOfOffset(refOffset) - 100, 0);
			int prefixOffset= document.getLineInformation(firstPrefixLine).getOffset();
			String prefix= document.get(prefixOffset, newOffset - prefixOffset);

			// Handle the indentation computation inside a temporary document
			Document temp= new Document(prefix + newText);
			DocumentRewriteSession session= temp.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
			scanner= new NescHeuristicScanner(temp);
			//indenter= new CIndenter(temp, scanner, fProject);
			installCPartitioner(temp);

			// Indent the first and second line
			// compute the relative indentation difference from the second line
			// (as the first might be partially selected) and use the value to
			// indent all other lines.
			boolean isIndentDetected= false;
			StringBuilder addition= new StringBuilder();
			int insertLength= 0;
			int first= document.computeNumberOfLines(prefix) + firstLine; // don't format first line
			int lines= temp.getNumberOfLines();
			boolean changed= false;

			for (int l= first; l < lines; l++) { // we don't change the number of lines while adding indents
				IRegion r= temp.getLineInformation(l);
				int lineOffset= r.getOffset();
				int lineLength= r.getLength();

				if (lineLength == 0) // don't modify empty lines
					continue;

				if (!isIndentDetected) {
					// indent the first pasted line
					String current= getCurrentIndent(temp, l, false);
					StringBuilder correct= new StringBuilder(computeIndent(temp, l, scanner));

					insertLength= subtractIndent(correct, current, addition);
					// workaround for bug 181139
					if (/*l != first && */temp.get(lineOffset, lineLength).trim().length() != 0) {
						isIndentDetected= true;
						if (insertLength == 0) {
							 // no adjustment needed, bail out
							if (firstLine == 0) {
								// but we still need to adjust the first line
								command.offset= newOffset;
								command.length= newLength;
								if (changed)
									break; // still need to get the leading indent of the first line
							}
							return;
						}
						removeCPartitioner(temp);
					} else {
						changed= insertLength != 0;
					}
				}

				// relatively indent all pasted lines
				if (insertLength > 0)
					addIndent(temp, l, addition, false);
				else if (insertLength < 0)
					cutIndent(temp, l, -insertLength, false);
			}

			temp.stopRewriteSession(session);
			newText= temp.get(prefix.length(), temp.getLength() - prefix.length());

			command.offset= newOffset;
			command.length= newLength;
			command.text= newText;
		} catch (BadLocationException e) {
			//CUIPlugin.log(e);
		}
	}
	
	private int getPeerPosition(IDocument document, DocumentCommand command) {
		if (document.getLength() == 0)
			return 0;
    	/*
    	 * Search for scope closers in the pasted text and find their opening peers
    	 * in the document.
    	 */
    	Document pasted= new Document(command.text);
    	installCPartitioner(pasted);
    	int firstPeer= command.offset;

    	NescHeuristicScanner pScanner= new NescHeuristicScanner(pasted);
    	NescHeuristicScanner dScanner= new NescHeuristicScanner(document);

    	// add scope relevant after context to peer search
    	int afterToken= dScanner.nextToken(command.offset + command.length, NescHeuristicScanner.UNBOUND);
    	try {
			switch (afterToken) {
			case Symbols.TokenRBRACE:
				pasted.replace(pasted.getLength(), 0, "}"); //$NON-NLS-1$
				break;
			case Symbols.TokenRPAREN:
				pasted.replace(pasted.getLength(), 0, ")"); //$NON-NLS-1$
				break;
			case Symbols.TokenRBRACKET:
				pasted.replace(pasted.getLength(), 0, "]"); //$NON-NLS-1$
				break;
			}
		} catch (BadLocationException e) {
			// cannot happen
			Assert.isTrue(false);
		}

    	int pPos= 0; // paste text position (increasing from 0)
    	int dPos= Math.max(0, command.offset - 1); // document position (decreasing from paste offset)
    	while (true) {
    		int token= pScanner.nextToken(pPos, NescHeuristicScanner.UNBOUND);
   			pPos= pScanner.getPosition();
    		switch (token) {
    			case Symbols.TokenLBRACE:
    			case Symbols.TokenLBRACKET:
    			case Symbols.TokenLPAREN:
    				pPos= skipScope(pScanner, pPos, token);
    				if (pPos == NescHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				break; // closed scope -> keep searching
    			case Symbols.TokenRBRACE:
    				int peer= dScanner.findOpeningPeer(dPos, '{', '}');
    				dPos= peer - 1;
    				if (peer == NescHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			case Symbols.TokenRBRACKET:
    				peer= dScanner.findOpeningPeer(dPos, '[', ']');
    				dPos= peer - 1;
    				if (peer == NescHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			case Symbols.TokenRPAREN:
    				peer= dScanner.findOpeningPeer(dPos, '(', ')');
    				dPos= peer - 1;
    				if (peer == NescHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			
    			/* for now no support for cases
    			 * TODO: Add support for switch statements	
    			case Symbols.TokenCASE:
    			case Symbols.TokenDEFAULT:
    			    {
    					CIndenter indenter= new CIndenter(document, dScanner, fProject);
    					peer= indenter.findReferencePosition(dPos, false, MatchMode.MATCH_CASE);
    					if (peer == NescHeuristicScanner.NOT_FOUND)
    						return firstPeer;
    					firstPeer= peer;
    				}
    				break; // keep searching
    			*/	
    			case Symbols.TokenEOF:
    				return firstPeer;
    			default:
    				// keep searching
    		}
    	}
    }
	
	/**
	 * Installs a C partitioner with <code>document</code>.
	 *
	 * @param document the document
	 */
	private static void installCPartitioner(Document document) {
		String[] types= new String[] {
		    INCPartitions.NC_MULTI_LINE_COMMENT,
			INCPartitions.NC_SINGLE_LINE_COMMENT,
			INCPartitions.NC_STRING,
			INCPartitions.NC_CHARACTER,
			INCPartitions.NC_PREPROCESSOR,
			IDocument.DEFAULT_CONTENT_TYPE
		};
		FastPartitioner partitioner= new FastPartitioner(new FastNescPartitionScanner(), types);
		partitioner.connect(document);
		document.setDocumentPartitioner(INCPartitions.NC_PARTITIONING, partitioner);
	}

	/**
	 * Installs a C partitioner with <code>document</code>.
	 *
	 * @param document the document
	 */
	private static void removeCPartitioner(Document document) {
		document.setDocumentPartitioner(INCPartitions.NC_PARTITIONING, null);
	}
	
	/**
     * Skips the scope opened by <code>token</code> in <code>document</code>,
     * returns either the position of the
     * @param pos
     * @param token
     * @return the position after the scope
     */
    private static int skipScope(NescHeuristicScanner scanner, int pos, int token) {
    	int openToken= token;
    	int closeToken;
    	switch (token) {
    		case Symbols.TokenLPAREN:
    			closeToken= Symbols.TokenRPAREN;
    			break;
    		case Symbols.TokenLBRACKET:
    			closeToken= Symbols.TokenRBRACKET;
    			break;
    		case Symbols.TokenLBRACE:
    			closeToken= Symbols.TokenRBRACE;
    			break;
    		default:
    			Assert.isTrue(false);
    			return -1; // dummy
    	}

    	int depth= 1;
    	int p= pos;

    	while (true) {
    		int tok= scanner.nextToken(p, NescHeuristicScanner.UNBOUND);
    		p= scanner.getPosition();

    		if (tok == openToken) {
    			depth++;
    		} else if (tok == closeToken) {
    			depth--;
    			if (depth == 0)
    				return p + 1;
    		} else if (tok == Symbols.TokenEOF) {
    			return NescHeuristicScanner.NOT_FOUND;
    		}
    	}
    }
	
	/**
	 * Indents line <code>line</code> in <code>document</code> with <code>indent</code>.
	 * Leaves leading comment signs alone.
	 *
	 * @param document the document
	 * @param line the line
	 * @param indent the indentation to insert
	 * @param indentInsideLineComments option whether to indent inside line comments starting at column 0
	 * @throws BadLocationException on concurrent document modification
	 */
	private static void addIndent(Document document, int line, CharSequence indent, boolean indentInsideLineComments) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int insert= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		if (indentInsideLineComments) {
			// go behind line comments
			while (insert < endOffset - 2 && document.get(insert, 2).equals(LINE_COMMENT))
				insert += 2;
		}

		// insert indent
		document.replace(insert, 0, indent.toString());
	}

	/**
	 * Cuts the visual equivalent of <code>toDelete</code> characters out of the
	 * indentation of line <code>line</code> in <code>document</code>. Leaves
	 * leading comment signs alone.
	 *
	 * @param document the document
	 * @param line the line
	 * @param toDelete the number of space equivalents to delete.
	 * @param indentInsideLineComments option whether to indent inside line comments starting at column 0
	 * @throws BadLocationException on concurrent document modification
	 */
	private void cutIndent(Document document, int line, int toDelete, boolean indentInsideLineComments) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int from= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		if (indentInsideLineComments) {
			// go behind line comments
			while (from < endOffset - 2 && document.get(from, 2).equals(LINE_COMMENT))
				from += 2;
		}

		int to= from;
		while (toDelete > 0 && to < endOffset) {
			char ch= document.getChar(to);
			if (!Character.isWhitespace(ch))
				break;
			toDelete -= computeVisualLength(ch);
			if (toDelete >= 0)
				to++;
			else
				break;
		}

		document.replace(from, to - from, null);
	}

	/**
	 * Computes the difference of two indentations and returns the difference in
	 * length of current and correct. If the return value is positive, <code>addition</code>
	 * is initialized with a substring of that length of <code>correct</code>.
	 *
	 * @param correct the correct indentation
	 * @param current the current indentation (migth contain non-whitespace)
	 * @param difference a string buffer - if the return value is positive, it will be cleared and set to the substring of <code>current</code> of that length
	 * @return the difference in lenght of <code>correct</code> and <code>current</code>
	 */
	private int subtractIndent(CharSequence correct, CharSequence current, StringBuilder difference) {
		int c1= computeVisualLength(correct);
		int c2= computeVisualLength(current);
		int diff= c1 - c2;
		if (diff <= 0)
			return diff;

		difference.setLength(0);
		int len= 0, i= 0;
		while (len < diff) {
			char c= correct.charAt(i++);
			difference.append(c);
			len += computeVisualLength(c);
		}

		return diff;
	}
	
	/**
	 * Returns the visual length of a given <code>CharSequence</code> taking into
	 * account the visual tabulator length.
	 *
	 * @param seq the string to measure
	 * @return the visual length of <code>seq</code>
	 */
	private int computeVisualLength(CharSequence seq) {
		int size= 0;
		int tablen= 4;

		for (int i= 0; i < seq.length(); i++) {
			char ch= seq.charAt(i);
			if (ch == '\t') {
				if (tablen != 0)
					size += tablen - size % tablen;
				// else: size stays the same
			} else {
				size++;
			}
		}
		return size;
	}
	
	/**
	 * Returns the visual length of a given character taking into
	 * account the visual tabulator length.
	 *
	 * @param ch the character to measure
	 * @return the visual length of <code>ch</code>
	 */
	private int computeVisualLength(char ch) {
		if (ch == '\t')
			return 4;
		return 1;
	}
	
	private boolean isClosedBrace(IDocument document, int offset, int length) {
		return getBlockBalance(document, offset, fPartitioning) <= 0;
	}
	
	/**
	 * Returns the block balance, i.e. zero if the blocks are balanced at
	 * <code>offset</code>, a negative number if there are more closing than opening
	 * braces, and a positive number if there are more opening than closing braces.
	 *
	 * @param document
	 * @param offset
	 * @param partitioning
	 * @return the block balance
	 */
	private static int getBlockBalance(IDocument document, int offset, String partitioning) {
		if (offset < 1)
			return -1;
		if (offset >= document.getLength())
			return 1;

		int begin = offset;
		int end = offset - 1;

		NescHeuristicScanner scanner = new NescHeuristicScanner(document);

		while (true) {
			begin = scanner.findOpeningPeer(begin - 1, '{', '}');
			end = scanner.findClosingPeer(end + 1, '{', '}');
			if (begin == -1 && end == -1)
				return 0;
			if (begin == -1)
				return -1;
			if (end == -1)
				return 1;
		}
	}
	
	/**
	 * Computes an insert position for an opening brace if <code>offset</code> maps to a position in
	 * <code>document</code> with a expression in parenthesis that will take a block after the closing parenthesis.
	 *
	 * @param document the document being modified
	 * @param offset the offset of the caret position, relative to the line start.
	 * @param partitioning the document partitioning
	 * @param max the max position
	 * @return an insert position relative to the line start if <code>line</code> contains a parenthesized expression that can be followed by a block, -1 otherwise
	 */
	private static int computeAnonymousPosition(IDocument document, int offset, String partitioning,  int max) {
		// find the opening parenthesis for every closing parenthesis on the current line after offset
		// return the position behind the closing parenthesis if it looks like a method declaration
		// or an expression for an if, while, for, catch statement

		NescHeuristicScanner scanner = new NescHeuristicScanner(document);
		int pos = offset;
		int length = max;
		int scanTo = scanner.scanForward(pos, length, '}');
		if (scanTo == -1)
			scanTo = length;

		int closingParen = findClosingParenToLeft(scanner, pos) - 1;

		while (true) {
			int startScan = closingParen + 1;
			closingParen = scanner.scanForward(startScan, scanTo, ')');
			if (closingParen == -1)
				break;

			int openingParen = scanner.findOpeningPeer(closingParen - 1, '(', ')');

			// no way an expression at the beginning of the document can mean anything
			if (openingParen < 1)
				break;

			// only select insert positions for parenthesis currently embracing the caret
			if (openingParen > pos)
				continue;
		}

		return -1;
	}
	
	/**
	 * Finds a closing parenthesis to the left of <code>position</code> in document, where that parenthesis is only
	 * separated by whitespace from <code>position</code>. If no such parenthesis can be found, <code>position</code> is returned.
	 *
	 * @param scanner the C heuristic scanner set up on the document
	 * @param position the first character position in <code>document</code> to be considered
	 * @return the position of a closing parenthesis left to <code>position</code> separated only by whitespace, or <code>position</code> if no parenthesis can be found
	 */
	private static int findClosingParenToLeft(NescHeuristicScanner scanner, int position) {
		if (position < 1)
			return position;

		if (scanner.previousToken(position - 1, NescHeuristicScanner.UNBOUND) == Symbols.TokenRPAREN)
			return scanner.getPosition() + 1;
		return position;
	}
	
	//from CDT IndentUtils
	public static String getCurrentIndent(IDocument document, int line, boolean indentInsideLineComments) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int from= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		int to= from;
		if (indentInsideLineComments) {
			// go behind line comments
			while (to < endOffset - 2 && document.get(to, 2).equals(LINE_COMMENT))
				to += 2;
		}
		
		while (to < endOffset) {
			char ch= document.getChar(to);
			if (!Character.isWhitespace(ch))
				break;
			to++;
		}

		return document.get(from, to - from);
	}
	
	public static String computeIndent(IDocument document, int line, NescHeuristicScanner scanner) throws BadLocationException {
		IRegion currentLine= document.getLineInformation(line);
		final int offset= currentLine.getOffset();
		// FIXME
		String indent= null;
		/*if (offset < document.getLength()) {
			ITypedRegion partition= TextUtilities.getPartition(document, INCPartitions.NC_PARTITIONING, offset, true);
			ITypedRegion startingPartition= TextUtilities.getPartition(document, INCPartitions.NC_PARTITIONING, offset, false);
			String type= partition.getType();
			if (type.equals(INCPartitions.NC_MULTI_LINE_COMMENT) || type.equals(INCPartitions.NC_MULTI_LINE_DOC_COMMENT)) {
				indent= computeCommentIndent(document, line, scanner, startingPartition);
			}  else if (startingPartition.getType().equals(INCPartitions.NC_PREPROCESSOR)) {
				indent= computePreprocessorIndent(document, line, startingPartition);
			}
		} */
		
		// standard C code indentation
		if (indent == null) {
			// TODO: FIXME
			StringBuilder computed= null; //computeIndentation(offset);
			if (computed != null)
				indent= computed.toString();
			else
				indent= new String();
		}
		return indent;
	}
}
