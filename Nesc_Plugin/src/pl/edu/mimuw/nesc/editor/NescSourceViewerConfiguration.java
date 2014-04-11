package pl.edu.mimuw.nesc.editor;

import java.util.Arrays;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

import pl.edu.mimuw.nesc.editor.contentassist.PreprocessorCompletionProcessor;
import pl.edu.mimuw.nesc.partitioning.INCPartitions;
import pl.edu.mimuw.nesc.scanners.NescWordDetector;

public class NescSourceViewerConfiguration extends TextSourceViewerConfiguration {

	private static final int AUTO_ACTIVATION_DELAY = 400;

	protected ITextEditor textEditor;

	protected RuleBasedScanner stringScanner;
	protected ITokenScanner multilineCommentScanner;
	protected ITokenScanner singlelineCommentScanner;
	protected RuleBasedScanner preprocessorScanner;
	protected RuleBasedScanner codeScanner;
	protected String documentPartitioning;

	protected final int scannerBufferSize = 10;

	public NescSourceViewerConfiguration(IPreferenceStore preferenceStore, ITextEditor editor, String partitioning) {
		super(preferenceStore);
		textEditor = editor;
		documentPartitioning = partitioning;
		initializeScanners();

	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler= new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		RuleBasedScanner scanner = getCodeScanner();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);

		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());
		reconciler.setDamager(dr, INCPartitions.NC_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, INCPartitions.NC_SINGLE_LINE_COMMENT);

		dr= new DefaultDamagerRepairer(getMultilineCommentScanner());
		reconciler.setDamager(dr, INCPartitions.NC_MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, INCPartitions.NC_MULTI_LINE_COMMENT);

		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, INCPartitions.NC_STRING);
		reconciler.setRepairer(dr, INCPartitions.NC_STRING);

		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, INCPartitions.NC_CHARACTER);
		reconciler.setRepairer(dr, INCPartitions.NC_CHARACTER);

		dr= new DefaultDamagerRepairer(getPreprocessorScanner());
		reconciler.setDamager(new PartitionDamager(), INCPartitions.NC_PREPROCESSOR);
		reconciler.setRepairer(dr, INCPartitions.NC_PREPROCESSOR);

		return reconciler;
	}

	public ITextEditor getEditor() {
		return textEditor;
	}

	protected void initializeScanners() {
		initializeStringScanner();
		initalizeSingelneCommentScanner();
		initializeMultilineCommentScanner();
	}

	protected void initializeStringScanner() {
		stringScanner = new BufferedRuleBasedScanner(scannerBufferSize);
		IToken textToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0x25, 0x88, 0x28)));
		IRule[] rules = { new SingleLineRule("\"", "\"", textToken),
				          new SingleLineRule("'", "'", textToken) };
		((BufferedRuleBasedScanner)stringScanner).setRules(rules);
		//IToken defaultToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0x00, 0x00, 0x00)));
		//((BufferedRuleBasedScanner)multilineCommentScanner).setDefaultReturnToken(defaultToken);
	}

	protected void initalizeSingelneCommentScanner() {
		singlelineCommentScanner = new BufferedRuleBasedScanner(scannerBufferSize);
		IToken textToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0xA5, 0xA8, 0xB8)));
		IRule[] rules = { new EndOfLineRule("//", textToken)};
		((BufferedRuleBasedScanner)singlelineCommentScanner).setRules(rules);
		//IToken defaultToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0x00, 0x00, 0x00)));
		//((BufferedRuleBasedScanner)multilineCommentScanner).setDefaultReturnToken(defaultToken);
	}

	protected void initializeMultilineCommentScanner() {
		multilineCommentScanner = new BufferedRuleBasedScanner(scannerBufferSize);
		IToken textToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0xA5, 0xA8, 0xB8)));
		IRule[] rules = { new MultiLineRule("/*", "*/", textToken, '\0', true) };
		((BufferedRuleBasedScanner)multilineCommentScanner).setRules(rules);
		//IToken defaultToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0x00, 0x00, 0x00)));
		//((BufferedRuleBasedScanner)multilineCommentScanner).setDefaultReturnToken(defaultToken);
	}

	protected RuleBasedScanner getStringScanner() {
		return stringScanner;
	}

	protected ITokenScanner getMultilineCommentScanner() {
		return multilineCommentScanner;
	}

	protected ITokenScanner getSinglelineCommentScanner() {
		return singlelineCommentScanner;
	}

	public void resetScanners() {
		preprocessorScanner = null;
		codeScanner = null;
	}

	protected RuleBasedScanner getPreprocessorScanner() {
		if (preprocessorScanner != null) {
			return preprocessorScanner;
		}
		IToken textToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0x9E, 0x53, 0xAE)));
		RuleBasedScanner scanner = new BufferedRuleBasedScanner(scannerBufferSize);
		IRule[] rules = { new EndOfLineRule("#", textToken, '\\', true) };
		scanner.setRules(rules);

		IToken defaultToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0x00, 0x00, 0x00)));
		scanner.setDefaultReturnToken(defaultToken);

		preprocessorScanner = scanner;
		return preprocessorScanner;
	}

	protected RuleBasedScanner getCodeScanner() {
		if (codeScanner != null) {
			return codeScanner;
		}
		RuleBasedScanner scanner = new BufferedRuleBasedScanner(scannerBufferSize);
		IWordDetector wordDetector = new NescWordDetector();
		WordRule words  = new WordRule(wordDetector);
		IToken keywordToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0x9E, 0x53, 0xAE)));
		// keywords
		words.addWord("as", keywordToken);
		words.addWord("abstract", keywordToken);
		words.addWord("async", keywordToken);
		words.addWord("atomic", keywordToken);
		words.addWord("call", keywordToken);
		words.addWord("command", keywordToken);
		words.addWord("component", keywordToken);
		words.addWord("components", keywordToken);
		words.addWord("configuration", keywordToken);
		words.addWord("event", keywordToken);
		words.addWord("extends", keywordToken);
		words.addWord("generic", keywordToken);
		words.addWord("implementation", keywordToken);
		words.addWord("interface", keywordToken);
		words.addWord("module", keywordToken);
		words.addWord("new", keywordToken);
		words.addWord("norace", keywordToken);
		words.addWord("post", keywordToken);
		words.addWord("provides", keywordToken);
		words.addWord("signal", keywordToken);
		words.addWord("task", keywordToken);
		words.addWord("uses", keywordToken);
		words.addWord("nx_struct", keywordToken);
		words.addWord("nx_union", keywordToken);
		/*
		 * c
		 */
		words.addWord("auto", keywordToken);
		words.addWord("break", keywordToken);
		words.addWord("case", keywordToken);
		words.addWord("char", keywordToken);
		words.addWord("const", keywordToken);
		words.addWord("continue", keywordToken);
		words.addWord("default", keywordToken);
		words.addWord("do", keywordToken);
		words.addWord("double", keywordToken);
		words.addWord("else", keywordToken);
		words.addWord("enum", keywordToken);
		words.addWord("extern", keywordToken);
		words.addWord("float", keywordToken);
		words.addWord("for", keywordToken);
		words.addWord("goto", keywordToken);
		words.addWord("if", keywordToken);
		words.addWord("inline", keywordToken);
		words.addWord("int", keywordToken);
		words.addWord("long", keywordToken);
		words.addWord("register", keywordToken);
		words.addWord("restrict", keywordToken);
		words.addWord("return", keywordToken);
		words.addWord("short", keywordToken);
		words.addWord("signed", keywordToken);
		words.addWord("sizeof", keywordToken);
		words.addWord("static", keywordToken);
		words.addWord("struct", keywordToken);
		words.addWord("switch", keywordToken);
		words.addWord("typedef", keywordToken);
		words.addWord("union", keywordToken);
		words.addWord("unsigned", keywordToken);
		words.addWord("void", keywordToken);
		words.addWord("volatile", keywordToken);
		words.addWord("while", keywordToken);
		/*
		 * GNU extensions
		 */
		words.addWord("asm", keywordToken);
		words.addWord("offsetof", keywordToken);
		words.addWord("__alignof__", keywordToken);
		words.addWord("__asm", keywordToken);
		words.addWord("__asm__", keywordToken);
		words.addWord("__attribute", keywordToken);
		words.addWord("__attribute__", keywordToken);
		words.addWord("__builtin_offsetof", keywordToken);
		words.addWord("__builtin_va_arg", keywordToken);
		words.addWord("__complex", keywordToken);
		words.addWord("__complex__", keywordToken);
		words.addWord("__const", keywordToken);
		words.addWord("__const__", keywordToken);
		words.addWord("__extension__", keywordToken);
		words.addWord("__imag", keywordToken);
		words.addWord("__imag__", keywordToken);
		words.addWord("__inline", keywordToken);
		words.addWord("__inline__", keywordToken);
		words.addWord("__label__", keywordToken);
		words.addWord("__real", keywordToken);
		words.addWord("__real__", keywordToken);
		words.addWord("__restrict", keywordToken);
		words.addWord("__signed", keywordToken);
		words.addWord("__signed__", keywordToken);
		words.addWord("__typeof", keywordToken);
		words.addWord("__typeof__", keywordToken);
		words.addWord("__volatile", keywordToken);
		words.addWord("__volatile__", keywordToken);

		IRule[] rules = { words,};
		scanner.setRules(rules);

		IToken defaultToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0x00, 0x00, 0x00)));
		scanner.setDefaultReturnToken(defaultToken);

		codeScanner = scanner;
		return scanner;
	}

	@Override
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "//", "//!", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return getIndentPrefixesForSpaces(4);
	}

	protected String[] getIndentPrefixesForSpaces(int tabWidth) {
		String[] indentPrefixes= new String[tabWidth + 2];
		indentPrefixes[0]= getStringWithSpaces(tabWidth);

		for (int i= 0; i < tabWidth; i++) {
			String spaces= getStringWithSpaces(i);
			if (i < tabWidth)
				indentPrefixes[i+1]= spaces + '\t';
			else
				indentPrefixes[i+1]= new String(spaces);
		}

		indentPrefixes[tabWidth + 1]= ""; //$NON-NLS-1$

		return indentPrefixes;
	}

	protected static String getStringWithSpaces(int count) {
		char[] spaceChars= new char[count];
		Arrays.fill(spaceChars, ' ');
		return new String(spaceChars);
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
				IDocument.DEFAULT_CONTENT_TYPE,
				INCPartitions.NC_MULTI_LINE_COMMENT,
				INCPartitions.NC_SINGLE_LINE_COMMENT,
				INCPartitions.NC_STRING,
				INCPartitions.NC_CHARACTER,
				INCPartitions.NC_PREPROCESSOR,
		};
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		final ContentAssistant contentAssistant = new ContentAssistant();
		final IContentAssistProcessor preprocessorAssistant = new PreprocessorCompletionProcessor();

		contentAssistant.enableAutoActivation(true);
		contentAssistant.setAutoActivationDelay(AUTO_ACTIVATION_DELAY);

		contentAssistant.setContentAssistProcessor(preprocessorAssistant, INCPartitions.NC_PREPROCESSOR);

		contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return contentAssistant;
	}

	/*@Override
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {

		final MultiPassContentFormatter formatter =
			new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer),
				IDocument.DEFAULT_CONTENT_TYPE);

		//formatter.setMasterStrategy(new CFormattingStrategy()); //TODO
		return formatter;
	}*/
	/*
	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		if (documentPartitioning != null)
			return documentPartitioning;
		return super.getConfiguredDocumentPartitioning(sourceViewer);
	}*/

}
