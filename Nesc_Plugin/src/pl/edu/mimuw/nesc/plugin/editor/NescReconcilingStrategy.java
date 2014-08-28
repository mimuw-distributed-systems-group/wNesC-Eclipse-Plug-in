package pl.edu.mimuw.nesc.plugin.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import pl.edu.mimuw.nesc.ProjectData;
import pl.edu.mimuw.nesc.plugin.frontend.FrontendManager;
import pl.edu.mimuw.nesc.plugin.marker.MarkerHelper;

/**
 *
 * @author Michał Szczepaniak <ms292534@students.mimuw.edu.pl>
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private final ITextEditor editor;
	private IProgressMonitor progressMonitor;
	private boolean reconciliationInProgress;

	public NescReconcilingStrategy(ITextEditor editor) {
		this.editor = editor;
		this.reconciliationInProgress = false;
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

		synchronized (this) {
			if (reconciliationInProgress) {
				return;
			}
			reconciliationInProgress = true;
			progressMonitor.beginTask("Reconciling " + path.toOSString(), IProgressMonitor.UNKNOWN);

			/* Use job and wait until it finishes. Jobs on workspace are
			 * synchronized, so we do not need to care about custom
			 * synchronization. */
			final Job job = new Job("Reconciling " + path) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					progressMonitor.beginTask("Reconciling " + path.toOSString(), IProgressMonitor.UNKNOWN);
					long start = System.currentTimeMillis();
					try {
						reconcile(project, file, path);
					} catch (CoreException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						reconciliationInProgress = false;
					}
					long end = System.currentTimeMillis();
					System.out.println("Reconciling done in " + (end - start) + "ms.");
					if (monitor != null) {
						monitor.done();
					}
					if (progressMonitor != null) {
						progressMonitor.done();
					}
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.SHORT);
			job.schedule();
		}
	}

	private void reconcile(IProject project, IFile file, IPath path) throws CoreException {
		final ProjectData projectData = FrontendManager.updateFile(project, path.toOSString());
		MarkerHelper.updateMarkers(project, file, projectData.getRootFileData());
		// TODO: update markers for all files.
	}
}
