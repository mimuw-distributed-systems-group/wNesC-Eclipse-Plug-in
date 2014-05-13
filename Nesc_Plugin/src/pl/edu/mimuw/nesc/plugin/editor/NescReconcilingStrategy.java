package pl.edu.mimuw.nesc.plugin.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Adapted from CDT.
 */
public class NescReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	private ITextEditor fEditor;
	private IProgressMonitor fProgressMonitor;
	// used by tests
	protected boolean fInitialProcessDone;
	
	public NescReconcilingStrategy(ITextEditor editor) {
		fEditor = editor;
		fInitialProcessDone = false;
	}

	@Override
	public void setDocument(IDocument document) {
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// only called for incremental reconciler
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}

	@Override
	public void reconcile(IRegion region) {
		reconcile(false);
	}

	private void reconcile(final boolean initialReconcile) {
		/**
		 * This code represents the way CDT handles reconciling.
		 * It is left here as a reference for designing the Nesc way
		 * of handling reconciling
		 */
		/*
		boolean computeAST= fEditor instanceof ICReconcilingListener;
		IASTTranslationUnit ast= null;
		IWorkingCopy workingCopy= fManager.getWorkingCopy(fEditor.getEditorInput());
		if (workingCopy == null) {
			return;
		}
		boolean forced= false;
		try {
			// reconcile
			synchronized (workingCopy) {
				forced= workingCopy.isConsistent();
				ast= workingCopy.reconcile(computeAST, true, fProgressMonitor);
			}
		} catch (OperationCanceledException e) {
			// document was modified while parsing
		} catch (CModelException e) {
			IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK,
					"Error in CDT UI during reconcile", e);  //$NON-NLS-1$
			CUIPlugin.log(status);
		} finally {
			if (computeAST) {
				IIndex index= null;
				if (ast != null) {
					index= ast.getIndex();
				}
				try {
					final boolean canceled = fProgressMonitor.isCanceled();
					if (ast == null || canceled) {
						((ICReconcilingListener)fEditor).reconciled(null, forced, fProgressMonitor);
					} else {
						((ASTTranslationUnit) ast).beginExclusiveAccess();
						try {
							((ICReconcilingListener)fEditor).reconciled(ast, forced, fProgressMonitor);
						} finally {
							((ASTTranslationUnit) ast).endExclusiveAccess();
						}
					}
					if (canceled) {
						aboutToBeReconciled();
					}
				} catch (Exception e) {
					IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK,
							"Error in CDT UI during reconcile", e);  //$NON-NLS-1$
					CUIPlugin.log(status);
				} finally {
					if (index != null) {
						index.releaseReadLock();
					}
				}
			}
		}
		*/
		System.err.println("preReconcile");
		if (!fInitialProcessDone) {
			// A good place to make sure that all the neccessary structures are in place
		}
		if (fEditor instanceof NescEditor) {
			try {
				NescEditor editor = (NescEditor)fEditor;
				editor.updateFileData();
			} catch (Exception e) {
				// For now we catch all exceptions so that the reconciling does not explode
				e.printStackTrace();
			}
		}
		System.err.println("postReconcile");
 	}

	@Override
	public void initialReconcile() {
		reconcile(true);
		fInitialProcessDone= true;
	}
}
