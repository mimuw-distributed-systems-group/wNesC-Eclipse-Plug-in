package pl.edu.mimuw.nesc.editor;

import java.util.ArrayList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.filesystem.URIUtil;

import pl.edu.mimuw.nesc.ContextRef;
import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.NescFrontend;
import pl.edu.mimuw.nesc.editor.highlighting.NescSyntaxHighlighter;
import pl.edu.mimuw.nesc.editor.util.NescUtil;
import pl.edu.mimuw.nesc.exception.InvalidOptionsException;
import pl.edu.mimuw.nesc.partitioning.FastNescPartitionScanner;
import pl.edu.mimuw.nesc.partitioning.FastNescPartitioner;
import pl.edu.mimuw.nesc.partitioning.INCPartitions;

public class NescEditor extends TextEditor {

	private final NescFrontend frontend;
	private ContextRef context = null;
	private NescSourceViewerConfiguration sourceViewerConfiguration = null;
	private static final IProgressMonitor monitor = new NullProgressMonitor();

	public NescEditor() {
		super();
		frontend = NescFrontend.builder().build();
		sourceViewerConfiguration = new NescSourceViewerConfiguration(null, this, INCPartitions.NC_PARTITIONING);
	}

	@Override
	public void createPartControl(Composite parent) {
		this.setSourceViewerConfiguration(sourceViewerConfiguration);
		super.createPartControl(parent);

		IDocumentPartitioner partitioner = new FastNescPartitioner();
		partitioner.connect(this.getDocument());
		this.getDocument().setDocumentPartitioner(partitioner);
		/*ISourceViewer sourceViewer = getSourceViewer();
		sourceViewer.getTextWidget().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				saveDocument();
			}
		});
		sourceViewer.getTextWidget().addLineStyleListener(new LineStyleListener() {

			public void lineGetStyle(LineStyleEvent event) {
				getLineStyle(event);
			}
		}); */
	}

	protected void saveDocument() {
		this.doSave(monitor);
		this.redrawViewer();
	}

	protected void getLineStyle(LineStyleEvent event) {
		int lineNum = -1;
		String filePath = getFileLocation();
		IDocument doc = getDocument();
		
		if (this.getOpenFileProject() != null) {
			System.err.println(this.getOpenFileProject().getName());
		} else {
			System.err.println("no project");
		}
		
		FileData fileData = NescUtil.getFileData(this.frontend, this.getNescContext(), filePath);

		if (fileData == null) {
			return;
		}

		if (filePath == null) {
			return;
		}

		try {
			lineNum = doc.getLineOfOffset(event.lineOffset) + 1;

			if (lineNum < 1)
				return;

			ArrayList<StyleRange> styles = new ArrayList<StyleRange>();

			NescSyntaxHighlighter.getInstance().colorizeLine(doc, lineNum, event.lineOffset, fileData, styles);

			if (styles.size() > 0) {
				StyleRange[] styleArray = new StyleRange[styles.size()];

				for (int i = 0; i < styles.size(); i++)
					styleArray[i] = styles.get(i);

				event.styles = styleArray;
			}

		} catch (BadLocationException e) {
			// The lineOffset provided for line number calculation was
			// incorrect, no colouring today :(
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

	public ContextRef getNescContext() {
		// TODO: Context needed!
		// Later we will want to get the context from the project,
		// For now we will create it every time for each file.

		if (context == null) {
			String file = this.getFileLocation();
			IPath filePath = URIUtil.toPath(URIUtil.toURI(file));
			String location = filePath.removeLastSegments(1).makeAbsolute().toOSString();
			String fileName = filePath.removeFileExtension().lastSegment();

			String options[] = { "-m", fileName, "-p", location };

			try {
				System.out.println("FRONTEND");
				context = frontend.createContext(options);
				System.out.println("FRONTEND END");
			} catch (InvalidOptionsException e) {
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if (context == null) {
			System.err.println("no context for you");
		}

		return context;
	}

	public void redrawViewer() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (getSourceViewer() != null)
					getSourceViewer().getTextWidget().redraw();
			}
		});

	}
}
