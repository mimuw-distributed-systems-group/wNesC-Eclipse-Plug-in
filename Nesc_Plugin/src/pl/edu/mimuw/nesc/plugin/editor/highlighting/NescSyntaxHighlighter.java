package pl.edu.mimuw.nesc.plugin.editor.highlighting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.ast.Location;
import pl.edu.mimuw.nesc.lexer.Comment;
import pl.edu.mimuw.nesc.preprocessor.directive.*;
import pl.edu.mimuw.nesc.token.*;

public class NescSyntaxHighlighter {

	/**
	 * This file contains parts of the SemanticHighlighting system. Eventually
	 *  it will be moved and adapted for this purpose.
	 * TODO: - check colour definition - fix colour scheme - find out how to
	 * connect to eclipse scheme system for better control over text
	 * presentation
	 * */

	private Color inactiveBlock;
	private Color preprocessorDirective;
	private Color preprocessorIncludeString;
	private Color preprocessorMacro;
	private Color commentBlock;
	private Color keyword;
	private Color stringConstant;
	private Color numberConstant;

	private final PreprocessorDirectiveHighlihter preprocessorHighlighter;
	private final SyntaxTokenHighlighter syntaxHighlighter;

	private static final NescSyntaxHighlighter instance = new NescSyntaxHighlighter();

	private NescSyntaxHighlighter() {
		preprocessorHighlighter = new PreprocessorDirectiveHighlihter();
		syntaxHighlighter = new SyntaxTokenHighlighter();

		loadColours();
	}

	protected void finalize() {
		if (inactiveBlock != null) {
			inactiveBlock.dispose();
		}
		if (preprocessorDirective != null) {
			preprocessorDirective.dispose();
		}
		if (preprocessorIncludeString != null) {
			preprocessorIncludeString.dispose();
		}
		if (preprocessorMacro != null) {
			preprocessorMacro.dispose();
		}
	}

	public void loadColours() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}

		inactiveBlock = new Color(display, 0x80, 0x80, 0x80);
		preprocessorDirective = new Color(display, 0x9E, 0x53, 0xAE);
		preprocessorIncludeString = new Color(display, 0xE1, 0x9D, 0x1E);
		preprocessorMacro = new Color(display, 0xE1, 0x9D, 0x1E);
		commentBlock = new Color(display, 0xA5, 0xA8, 0xB8);
		stringConstant = new Color(display, 0x25, 0x88, 0x28);
		numberConstant = new Color(display, 0x30, 0x30, 0xD0);
		keyword = new Color(display, 0x47, 0x30, 0xBB);
		
		
	}

	public static NescSyntaxHighlighter getInstance() {
		return instance;
	}

	public void colorizeLine(IDocument doc, Integer lineNum, Integer lineDocOffset, FileData fileData,
			List<StyleRange> styles) {
		List<PreprocessorDirective> directives = fileData.getPreprocessorDirectives();
		boolean inactiveBlock = false;
		
		if (directives.size() > 0) {
			PreprocessorDirective previousDirective = directives.get(0);
	
			/**
			 * First we highlight the preprocessor directives and inactive
			 * preprocessor blocks
			**/ 
			for (PreprocessorDirective directive : directives) {
				PreprocessorDirective.LineRange range = directive.getLineRange();
				if (range.getStart() > lineNum && !previousDirective.isActiveBlock() && directive != previousDirective) {
					styles.add(getInactiveBlockStyle(doc, lineNum, lineDocOffset));
					inactiveBlock = true;
				} else if (range.getStart() <= lineNum && range.getEnd() >= lineNum) {
					styles.addAll(getDirectiveStyle(directive, lineDocOffset));
				} else if (range.getEnd() < lineNum) {
					previousDirective = directive;
					continue;
				}
				break;
			}
		}

		if (!inactiveBlock) {
			styles.addAll(getCommentStyle(fileData.getComments(), lineNum, lineDocOffset));

			if (fileData.getTokens().containsKey(lineNum)) {
				for (Token tok : fileData.getTokens().get(lineNum)) {
					styles.addAll(tok.accept(syntaxHighlighter, lineDocOffset));
				}
			}
		}
		
		Collections.sort(styles, new Comparator<StyleRange>() {
			@Override
			public int compare(StyleRange first, StyleRange second) {
				return Integer.compare(first.start, second.start);
			}
		});
	}

	private StyleRange getInactiveBlockStyle(IDocument doc, Integer lineNum, Integer lineOffset) {
		try {
			Integer lineLength = doc.getLineLength(lineNum - 1);
			return new StyleRange(lineOffset, lineLength, inactiveBlock, null);
		} catch (BadLocationException b) {
			// We received a bad line number. This should not happen under
			// normal circumstances.
			return null;
		}
	}

	private List<StyleRange> getDirectiveStyle(PreprocessorDirective directive, Integer offset) {
		return directive.accept(preprocessorHighlighter, offset);
	}

	private List<StyleRange> getCommentStyle(List<Comment> comments, Integer lineNum, Integer offset) {
		List<StyleRange> styles = new ArrayList<>();
		for (Comment comment : comments) {
			Location commentLocation = comment.getLocation();
			if (comment.isC()) {
				String commentLinesParts[] = comment.getBody().split("\n");
				int commentLinesCount = commentLinesParts.length;
				if (commentLocation.getLine() <= lineNum && commentLocation.getLine() + commentLinesCount > lineNum) {
					int commentFileOffset = offset;
					if (lineNum == commentLocation.getLine()) {
						commentFileOffset += commentLocation.getColumn() - 1;
					}
					int commentLine = lineNum - commentLocation.getLine();
					styles.add(new StyleRange(commentFileOffset, commentLinesParts[commentLine].length(),
							commentBlock, null));
				}
			} else {
				if (comment.getLocation().getLine() == lineNum) {
					styles.add(new StyleRange(offset + commentLocation.getColumn() - 1, comment.getBody().length(),
							commentBlock, null));
				}
			}
		}
		return styles;
	}

	private class SyntaxTokenHighlighter implements Token.Visitor<List<StyleRange>, Integer> {

		@Override
		public List<StyleRange> visit(CharacterToken token, Integer offset) {
			List<StyleRange> styles = new ArrayList<>();

			styles.add(new StyleRange(offset + token.getStartLocation().getColumn() - 1, token.getValue().length() + 2,
					stringConstant, null));

			return styles;
		}

		@Override
		public List<StyleRange> visit(NumberToken token, Integer offset) {
			List<StyleRange> styles = new ArrayList<>();

			styles.add(new StyleRange(offset + token.getStartLocation().getColumn() -1, token.getValue().length(),
					numberConstant, null));

			return styles;
		}

		@Override
		public List<StyleRange> visit(StringToken token, Integer offset) {
			List<StyleRange> styles = new ArrayList<>();

			styles.add(new StyleRange(offset + token.getStartLocation().getColumn() - 1,
					token.getEndLocation().getColumn() - token.getStartLocation().getColumn() + 1,
					stringConstant, null));

			return styles;
		}

		@Override
		public List<StyleRange> visit(KeywordToken token, Integer offset) {
			List<StyleRange> styles = new ArrayList<>();

			styles.add(new StyleRange(offset + token.getStartLocation().getColumn() - 1, token.getText().length(),
					keyword, null));

			return styles;
		}

		@Override
		public List<StyleRange> visit(PunctuationToken token, Integer offset) {
			return new ArrayList<>();
		}

		@Override
		public List<StyleRange> visit(IdToken token, Integer offset) {
			return new ArrayList<>();
		}

	}

	private class PreprocessorDirectiveHighlihter implements PreprocessorDirective.Visitor<List<StyleRange>, Integer> {

		private ArrayList<StyleRange> directiveStyles(PreprocessorDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = new ArrayList<>();

			styles.add(new StyleRange(offset + directive.getHashLocation().getColumn() - 1, directive.getHashLocation()
					.getLength(), preprocessorDirective, null));

			styles.add(new StyleRange(offset + directive.getKeywordLocation().getColumn() - 1, directive
					.getKeywordLocation().getLength(), preprocessorDirective, null));

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(IncludeDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			styles.add(new StyleRange(offset + directive.getArgumentLocation().getColumn() - 1, directive
					.getArgumentLocation().getLength(), preprocessorIncludeString, null));

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(DefineDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			styles.add(new StyleRange(offset + directive.getNameLocation().getColumn() - 1, directive.getNameLocation()
					.getLength(), preprocessorMacro, null));

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(UndefDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			styles.add(new StyleRange(offset + directive.getNameLocation().getColumn() - 1, directive.getNameLocation()
					.getLength(), preprocessorMacro, null));

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(IfDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(IfdefDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			styles.add(new StyleRange(offset + directive.getMacroLocation().getColumn() - 1, directive
					.getMacroLocation().getLength(), preprocessorMacro, null));

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(IfndefDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			styles.add(new StyleRange(offset + directive.getMacroLocation().getColumn() - 1, directive
					.getMacroLocation().getLength(), preprocessorMacro, null));

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(ElseDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(ElifDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(EndifDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(ErrorDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(WarningDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(PragmaDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(LineDirective directive, Integer offset) {
			ArrayList<StyleRange> styles = directiveStyles(directive, offset);

			return styles;
		}

		@Override
		public ArrayList<StyleRange> visit(UnknownDirective directive, Integer offset) {
			return new ArrayList<>();
		}

	}

}
