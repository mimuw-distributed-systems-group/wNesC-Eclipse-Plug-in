package pl.edu.mimuw.nesc.plugin.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.plugin.marker.MarkerHelper;
import pl.edu.mimuw.nesc.plugin.projects.util.ProjectManager;

/**
 *
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private final ITextEditor editor;
	private IProgressMonitor progressMonitor;

	public NescReconcilingStrategy(ITextEditor editor) {
		this.editor = editor;
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		progressMonitor = monitor;
	}

	@Override
	public void setDocument(IDocument document) {
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// only called for incremental reconciler
		reconcile();
	}

	@Override
	public void reconcile(IRegion region) {
		reconcile();
	}

	@Override
	public void initialReconcile() {
		reconcile();
	}

	private void reconcile() {
		long start = System.currentTimeMillis();
		System.out.println("Start reconciling preparation...");
		if (!(editor instanceof NescEditor)) {
			return;
		}
		final NescEditor nescEditor = (NescEditor) editor;
		final IProject project = nescEditor.getOpenFileProject();
		if (project == null) {
			return;
		}
		final IEditorInput input = nescEditor.getEditorInput();
		if (input == null || !(input instanceof FileEditorInput)) {
			return;
		}
		final IFile file = ((FileEditorInput) input).getFile();
		final IPath path = file.getRawLocation();

		/* All necessary data is obtained. */

		progressMonitor.beginTask("Reconciling " + path.toOSString(), IProgressMonitor.UNKNOWN);

		try {
			reconcile(project, file, path);
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// Prevents from killing the reconciler thread when an unexpected
			// exception is thrown.
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("Reconciling done in " + (end - start) + "ms.");
		if (progressMonitor != null) {
			progressMonitor.done();
		}
	}

	private void reconcile(IProject project, IFile file, IPath path) throws CoreException {
		final FileData data = ProjectManager.updateFile(project, path.toOSString());
		MarkerHelper.updateMarkers(project, file, data);
	}
}
