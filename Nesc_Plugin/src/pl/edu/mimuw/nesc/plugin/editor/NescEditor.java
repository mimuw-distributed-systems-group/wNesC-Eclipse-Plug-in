package pl.edu.mimuw.nesc.plugin.editor;

import java.util.Stack;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import pl.edu.mimuw.nesc.plugin.editor.util.NescHeuristicScanner;
import pl.edu.mimuw.nesc.plugin.editor.util.Symbols;
import pl.edu.mimuw.nesc.plugin.partitioning.FastNescPartitioner;
import pl.edu.mimuw.nesc.plugin.partitioning.INCPartitions;

public class NescEditor extends TextEditor {

	public static final String EDITOR_ID = "pl.edu.mimuw.nesc.NesCEditor";

	private NescSourceViewerConfiguration sourceViewerConfiguration = null;

	private class ExitPolicy implements IExitPolicy {
		final char fExitCharacter;
		final char fEscapeCharacter;
		final Stack<BracketLevel> fStack;
		final int fSize;

		public ExitPolicy(char exitCharacter, char escapeCharacter, Stack<BracketLevel> stack) {
			fExitCharacter = exitCharacter;
			fEscapeCharacter = escapeCharacter;
			fStack = stack;
			fSize = fStack.size();
		}

		@Override
		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
			if (fSize == fStack.size() && !isMasked(offset)) {
				if (event.character == fExitCharacter) {
					BracketLevel level = fStack.peek();
					if (level.fFirstPosition.offset > offset || level.fSecondPosition.offset < offset)
						return null;
					if (level.fSecondPosition.offset == offset && length == 0)
						// don't enter the character if if its the closing peer
						return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
				}
				// when entering an anonymous class between the parenthesis', we don't want
				// to jump after the closing parenthesis when return is pressed
				if (event.character == SWT.CR && offset > 0) {
					IDocument document = getSourceViewer().getDocument();
					try {
						if (document.getChar(offset - 1) == '{')
							return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
					} catch (BadLocationException e) {
					}
				}
			}
			return null;
		}

		private boolean isMasked(int offset) {
			IDocument document = getSourceViewer().getDocument();
			try {
				return fEscapeCharacter == document.getChar(offset - 1);
			} catch (BadLocationException e) {
			}
			return false;
		}
	}

	private static class BracketLevel {
		LinkedModeUI fUI;
		Position fFirstPosition;
		Position fSecondPosition;
	}

	private static class ExclusivePositionUpdater implements IPositionUpdater {

		/** The position category. */
		private final String fCategory;

		/**
		 * Creates a new updater for the given <code>category</code>.
		 *
		 * @param category the new category.
		 */
		public ExclusivePositionUpdater(String category) {
			fCategory = category;
		}

		@Override
		public void update(DocumentEvent event) {
			int eventOffset = event.getOffset();
			int eventOldLength = event.getLength();
			int eventNewLength = event.getText() == null ? 0 : event.getText().length();
			int deltaLength = eventNewLength - eventOldLength;

			try {
				Position[] positions = event.getDocument().getPositions(fCategory);

				for (int i = 0; i != positions.length; i++) {

					Position position = positions[i];

					if (position.isDeleted())
						continue;

					int offset = position.getOffset();
					int length = position.getLength();
					int end = offset + length;

					if (offset >= eventOffset + eventOldLength) {
						// position comes
						// after change - shift
						position.setOffset(offset + deltaLength);
				    } else if (end <= eventOffset) {
						// position comes way before change -
						// leave alone
					} else if (offset <= eventOffset && end >= eventOffset + eventOldLength) {
						// event completely internal to the position - adjust length
						position.setLength(length + deltaLength);
					} else if (offset < eventOffset) {
						// event extends over end of position - adjust length
						int newEnd = eventOffset;
						position.setLength(newEnd - offset);
					} else if (end > eventOffset + eventOldLength) {
						// event extends from before position into it - adjust offset
						// and length
						// offset becomes end of event, length adjusted accordingly
						int newOffset = eventOffset + eventNewLength;
						position.setOffset(newOffset);
						position.setLength(end - newOffset);
					} else {
						// event consumes the position - delete it
						position.delete();
					}
				}
			} catch (BadPositionCategoryException e) {
				// ignore and return
			}
		}

	}

	private class BracketInserter implements VerifyKeyListener, ILinkedModeListener {

		private final String CATEGORY = toString();
		private IPositionUpdater fUpdater = new ExclusivePositionUpdater(CATEGORY);
		private Stack<BracketLevel> fBracketLevelStack = new Stack<BracketLevel>();

		@Override
		public void verifyKey(VerifyEvent event) {
			// early pruning to slow down normal typing as little as possible
			if (!event.doit || getInsertMode() != SMART_INSERT) {
				return;
			}
			switch (event.character) {
				case '(':
				case '<':
				case '[':
				case '\'':
				case '\"':
					break;
				default:
					return;
			}

			final ISourceViewer sourceViewer = getSourceViewer();
			IDocument document = sourceViewer.getDocument();

			final Point selection = sourceViewer.getSelectedRange();
			final int offset = selection.x;
			final int length = selection.y;
			try {
				IRegion startLine = document.getLineInformationOfOffset(offset);
				IRegion endLine = document.getLineInformationOfOffset(offset + length);
				if (startLine != endLine && isBlockSelectionModeEnabled()) {
					return;
				}

				ITypedRegion partition = TextUtilities.getPartition(document, INCPartitions.NC_PARTITIONING, offset, true);
				if (!IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())
						&& !INCPartitions.NC_PREPROCESSOR.equals(partition.getType())) {
					return;
				}
				
				NescHeuristicScanner scanner = new NescHeuristicScanner(document, INCPartitions.NC_PARTITIONING, partition.getType());
				int nextToken = scanner.nextToken(offset + length, endLine.getOffset() + endLine.getLength());
				String next = nextToken == Symbols.TokenEOF ? null : document.get(offset, scanner.getPosition() - offset).trim();
				int prevToken = scanner.previousToken(offset - 1, startLine.getOffset());
				int prevTokenOffset = scanner.getPosition() + 1;
				String previous = prevToken == Symbols.TokenEOF ? null : document.get(prevTokenOffset, offset - prevTokenOffset).trim();

				switch (event.character) {
					case '(':
						if (nextToken == Symbols.TokenLPAREN
								|| nextToken == Symbols.TokenIDENT
								|| next != null && next.length() > 1)
							return;
						break;

					case '<':
						if (nextToken == Symbols.TokenLESSTHAN
								|| nextToken == Symbols.TokenGREATERTHAN
								|| prevToken != Symbols.TokenIDENT
								|| !scanner.looksLikeInterface(offset, startLine.getOffset() -1)) {
							return;
						}
						break;

					case '[':
						if (nextToken == Symbols.TokenIDENT
								|| next != null && next.length() > 1)
							return;
						break;

					case '\'':
					case '"':
						if (nextToken == Symbols.TokenIDENT
								|| next != null && (next.length() > 1 || next.charAt(0) == event.character))
							return;
						break;

					default:
						return;
				}

				if (!validateEditorInputState()) {
					return;
				}

				final char character = event.character;
				final char closingCharacter = getPeerCharacter(character);
				final StringBuilder buffer = new StringBuilder(3);
				buffer.append(character);
				buffer.append(closingCharacter);

				document.replace(offset, length, buffer.toString());

				BracketLevel level = new BracketLevel();
				fBracketLevelStack.push(level);

				LinkedPositionGroup group = new LinkedPositionGroup();
				group.addPosition(new LinkedPosition(document, offset + 1, 0, LinkedPositionGroup.NO_STOP));

				LinkedModeModel model = new LinkedModeModel();
				model.addLinkingListener(this);
				model.addGroup(group);
				model.forceInstall();

				// Set up position tracking for our magic peers
				if (fBracketLevelStack.size() == 1) {
					document.addPositionCategory(CATEGORY);
					document.addPositionUpdater(fUpdater);
				}
				level.fFirstPosition = new Position(offset, 1);
				level.fSecondPosition = new Position(offset + 1, 1);
				document.addPosition(CATEGORY, level.fFirstPosition);
				document.addPosition(CATEGORY, level.fSecondPosition);

				level.fUI = new EditorLinkedModeUI(model, sourceViewer);
				level.fUI.setSimpleMode(true);
				level.fUI.setExitPolicy(new ExitPolicy(closingCharacter, getEscapeCharacter(closingCharacter), fBracketLevelStack));
				level.fUI.setExitPosition(sourceViewer, offset + 2, 0, Integer.MAX_VALUE);
				level.fUI.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
				level.fUI.enter();

				IRegion newSelection = level.fUI.getSelectedRegion();
				sourceViewer.setSelectedRange(newSelection.getOffset(), newSelection.getLength());

				event.doit = false;
			} catch (BadLocationException e) {
			} catch (BadPositionCategoryException e) {
			}
		}

		@Override
		public void left(LinkedModeModel environment, int flags) {

			final BracketLevel level = fBracketLevelStack.pop();

			if (flags != ILinkedModeListener.EXTERNAL_MODIFICATION)
				return;

			// remove brackets
			final ISourceViewer sourceViewer = getSourceViewer();
			final IDocument document = sourceViewer.getDocument();
			if (document instanceof IDocumentExtension) {
				IDocumentExtension extension = (IDocumentExtension) document;
				extension.registerPostNotificationReplace(null, new IDocumentExtension.IReplace() {

					@Override
					public void perform(IDocument d, IDocumentListener owner) {
						if ((level.fFirstPosition.isDeleted || level.fFirstPosition.length == 0)
								&& !level.fSecondPosition.isDeleted
								&& level.fSecondPosition.offset == level.fFirstPosition.offset) {
							try {
								document.replace(level.fSecondPosition.offset,
												 level.fSecondPosition.length,
												 null);
							} catch (BadLocationException e) {
								//CUIPlugin.log(e);
							}
						}

						if (fBracketLevelStack.size() == 0) {
							document.removePositionUpdater(fUpdater);
							try {
								document.removePositionCategory(CATEGORY);
							} catch (BadPositionCategoryException e) {
								//CUIPlugin.log(e);
							}
						}
					}
				});
			}
		}

		@Override
		public void suspend(LinkedModeModel environment) {
		}

		@Override
		public void resume(LinkedModeModel environment, int flags) {
		}
	}

	public NescEditor() {
		super();
		sourceViewerConfiguration = new NescSourceViewerConfiguration(null, this, INCPartitions.NC_PARTITIONING);
	}

	@Override
	public void createPartControl(Composite parent) {
		this.setSourceViewerConfiguration(sourceViewerConfiguration);
		super.createPartControl(parent);

		IDocumentPartitioner partitioner = new FastNescPartitioner();
		partitioner.connect(this.getDocument());
		this.getDocument().setDocumentPartitioner(partitioner);

		this.configureInsertMode(SMART_INSERT, true);
		this.setInsertMode(SMART_INSERT);

		if (this.getSourceViewer() instanceof ITextViewerExtension) {
			((ITextViewerExtension)this.getSourceViewer()).prependVerifyKeyListener(new BracketInserter());
		}
	}

	public IDocument getDocument() {
		return getSourceViewer().getDocument();
	}

	public String getFileLocation() {
		IPath path = null;

		if (this.getEditorInput() == null) {
			return null;
		}

		if (this.getEditorInput() instanceof FileStoreEditorInput) {
			FileStoreEditorInput fsei = (FileStoreEditorInput) this.getEditorInput();
			path = URIUtil.toPath(fsei.getURI());
		} else {
			IResource resource = (IResource) this.getEditorInput().getAdapter(IResource.class);
			if (resource != null && resource.getType() == IResource.FILE) {
				path = resource.getRawLocation();
			}
		}

		if (path != null) {
			return path.makeAbsolute().toOSString();
		}
		return null;
	}

	public IProject getOpenFileProject() {
		IResource resource = (IResource) this.getEditorInput().getAdapter(IResource.class);
		if (resource != null) {
			return resource.getProject();
		}
		return null;
	}

	private static char getPeerCharacter(char character) {
		switch (character) {
			case '(':
				return ')';

			case ')':
				return '(';

			case '<':
				return '>';

			case '>':
				return '<';

			case '[':
				return ']';

			case ']':
				return '[';

			case '"':
				return character;

			case '\'':
				return character;

			default:
				throw new IllegalArgumentException();
		}
	}

	private static char getEscapeCharacter(char character) {
		switch (character) {
			case '"':
			case '\'':
				return '\\';
			default:
				return 0;
		}
	}
}
